from flask import Flask, request, jsonify
from icecream import ic
import os
import json

app = Flask(__name__)


processed_logs = set()

def has_valid_timestamp(entry):
    try:
        timestamp = int(entry.split()[-1])
        return True
    except ValueError:
        return False

def processData(data):
    # Ensure data is a list
    if not isinstance(data, list):
        ic("Invalid data format, expected a list")
        return


    valid_data = [entry for entry in data if has_valid_timestamp(entry)]
    sorted_data = sorted(valid_data, key=lambda x: int(x.split()[-1]))

    with open('node_data.txt', 'a') as f:
        for item in sorted_data:
            if item not in processed_logs:
                f.write(item + '\n')
                processed_logs.add(item)
        f.write('=' * 50 + '\n')

@app.route("/", methods=['POST'])
def index():
    if request.method == 'POST':
        try:
            data = request.json
            ic(data)
            processData(data)
            return jsonify({"status": "success"}), 200
        except Exception as e:
            ic(f"Error processing request: {e}")
            return jsonify({"status": "error", "message": str(e)}), 500
    return jsonify({"status": "method not allowed"}), 405

if __name__ == '__main__':
    if not os.path.isfile("node_data.txt"):
        ic('File doesn\'t exist, creating ...')
        with open('node_data.txt', 'w') as f:
            pass
    app.run(debug=True, host="0.0.0.0")
