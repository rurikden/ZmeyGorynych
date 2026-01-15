# cursor_proxy.py - простой прокси для Cursor
from flask import Flask, request, jsonify
import requests
import json
import logging

app = Flask(__name__)
LM_STUDIO_URL = "http://localhost:1234/v1"

# Включаем логирование
logging.basicConfig(level=logging.INFO)

@app.route('/v1/chat/completions', methods=['POST'])
def chat_completions():
    try:
        data = request.json
        app.logger.info(f"Получен запрос от Cursor: {json.dumps(data, indent=2)}")
        
        # Форсируем нужные параметры для совместимости
        if data:
            data['stream'] = False  # Отключаем streaming
            if 'temperature' not in data:
                data['temperature'] = 0.1  # Низкая температура для кода
        
        # Отправляем в LM Studio
        response = requests.post(
            f"{LM_STUDIO_URL}/chat/completions",
            headers=request.headers,
            json=data,
            timeout=120
        )
        
        app.logger.info(f"Ответ от LM Studio: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            
            # ОЧЕНЬ ВАЖНО: Приводим ответ к формату, который понимает Cursor
            # Удаляем все лишние поля
            clean_result = {
                "id": result.get("id", "chatcmpl-local"),
                "object": "chat.completion",
                "created": result.get("created", 1677652288),
                "model": "gpt-3.5-turbo",  # Фиксируем имя модели
                "choices": [],
                "usage": result.get("usage", {"prompt_tokens": 0, "completion_tokens": 0, "total_tokens": 0})
            }
            
            # Обрабатываем choices
            if "choices" in result and len(result["choices"]) > 0:
                for choice in result["choices"]:
                    clean_choice = {
                        "index": choice.get("index", 0),
                        "message": {
                            "role": "assistant",
                            "content": ""
                        },
                        "finish_reason": choice.get("finish_reason", "stop")
                    }
                    
                    # Извлекаем контент
                    if "message" in choice and "content" in choice["message"]:
                        clean_choice["message"]["content"] = choice["message"]["content"]
                    elif "text" in choice:
                        clean_choice["message"]["content"] = choice["text"]
                    elif "delta" in choice and "content" in choice["delta"]:
                        clean_choice["message"]["content"] = choice["delta"]["content"]
                    else:
                        # Если контента нет, используем fallback
                        clean_choice["message"]["content"] = "Ответ получен"
                    
                    clean_result["choices"].append(clean_choice)
            else:
                # Fallback если choices нет
                clean_result["choices"] = [{
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "Ошибка: неверный формат ответа"
                    },
                    "finish_reason": "stop"
                }]
            
            app.logger.info("Очищенный ответ для Cursor готов")
            return jsonify(clean_result)
        
        return jsonify({"error": "Ошибка LM Studio"}), response.status_code
        
    except Exception as e:
        app.logger.error(f"Ошибка в прокси: {str(e)}")
        return jsonify({
            "error": str(e),
            "choices": [{
                "message": {
                    "role": "assistant",
                    "content": f"Ошибка прокси: {str(e)}"
                }
            }]
        }), 500

@app.route('/v1/models', methods=['GET'])
def models():
    try:
        response = requests.get(f"{LM_STUDIO_URL}/models")
        if response.status_code == 200:
            data = response.json()
            # Упрощаем ответ
            clean_data = {
                "object": "list",
                "data": [{
                    "id": "gpt-3.5-turbo",
                    "object": "model",
                    "owned_by": "openai"
                }]
            }
            return jsonify(clean_data)
        return jsonify({"object": "list", "data": []})
    except:
        return jsonify({"object": "list", "data": []})

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "ok", "proxy": "running"})

if __name__ == '__main__':
    print("=" * 50)
    print("Прокси-сервер для Cursor запускается...")
    print("Порт: 5000")
    print(f"Перенаправление на: {LM_STUDIO_URL}")
    print("=" * 50)
    app.run(port=5000, debug=True, use_reloader=False)