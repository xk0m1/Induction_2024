from flask import Flask,request,jsonify
from icecream import ic
import os

app = Flask(__name__)

def processData(data):
    with open('node_data.txt', 'a') as f:
        for item in data:
            f.write(item + '\n')
        f.write('=' * 50 + '\n')

@app.route("/",methods=['POST'])
def index():
    if request.method == 'POST':
        data = request.json
        ic(data)
        processData(data)
    return 'Working'

if __name__ == '__main__':
    if(not os.path.isfile("node_data.txt")):
        ic('File doesnt exist, creating ...')
        with open('node_data.txt','w') as f:
            pass
    app.run(debug=True,host="0.0.0.0")
