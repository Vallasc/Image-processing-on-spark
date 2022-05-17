from bottle import route, request, run, static_file, FileUpload
import time
import os
import subprocess
import psutil

save_path = "./data"

spark_process = None

def kill(proc_pid):
    process = psutil.Process(proc_pid)
    for proc in process.children(recursive=True):
        proc.kill()
    process.kill()

def millis():
    return int(round(time.time() * 1000))

@route('/')
def root():
    return static_file('./web/public/index.html', root='.')

@route('/data/<filename>')
def serve_files(filename):
    try:
        return static_file(filename, root = './data/')
    except: 
        print(dir + "/" + filename + " not found")


@route('/upload', method='POST')
def do_work():
    # Insert num worker
    # Worker memory
    try:
        kill(spark_process.pid)
        time.sleep(20)
    except:
        print("Error killing process")

    upload :FileUpload = request.files.get('image')
    name, ext = os.path.splitext(upload.filename)
    if ext not in ('.png', '.jpg', '.jpeg'):
        return "File extension not allowed."

    file_name = str(millis()) + ext
    file_input_path = "{path}/{file}".format(path=save_path, file=file_name)
    upload.save(file_input_path)
    
    file_out_path = "{path}/out_{file}".format(path=save_path, file=file_name)
    file_json = file_out_path + ".json"
    command = ["spark-submit", "--class", "SparkJob", "./jar/binary.jar",
                "--output_file_json", file_json, "--output_file_image", file_out_path, file_input_path]
    print(command)
    spark_process = subprocess.Popen(command, shell = True)
    return file_out_path

if __name__ == '__main__':
    run(host='0.0.0.0', port=9090)