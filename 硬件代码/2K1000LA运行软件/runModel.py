import torch
import torch.nn as nn
import serial
import datetime
import time
import re

#System init
BLE_Master_port  = "/dev/ttyS4"  # 蓝牙主设备，与检测器通信
BLE_Slave_port   = "/dev/ttyS3"  # 蓝牙从设备，与手机通信
NET_port         = "/dev/ttyS0"  # 与网络命模块连接
UART_MAX_SIZE = 36

oldResistance = 0

# 获取当前时间戳（自 1970 年 1 月 1 日以来的秒数）
start_timestamp = time.time()

def extract_first_float(s):
    # 正则表达式匹配以#开头，以;结尾的字符串中的浮点数
    pattern = r'#([-+]?\d*\.?\d+);'
    match = re.search(pattern, s)
    if match:
        # 提取第一个捕获组的内容，并转换为浮点数
        return float(match.group(1))
    else:
        return None  # 如果没有找到匹配项，返回None

def send_data_to_serial_port(serial_port, data):
    try:
        ser = serial.Serial(serial_port, 9600)  # 9600为波特率，可以根据需要修改
        ser.write(data.encode())                # 将数据编码并发送到串口
        ser.close()
        return True
    except serial.SerialException as e:
        print(f"Error while sending data to serial port: {str(e)}")
        return False

def receive_data_from_serial_port(serial_port, max_bytes_to_read):
    try:
        ser = serial.Serial(serial_port, 9600)      # 9600为波特率，可以根据需要修改
        data = ser.read(max_bytes_to_read)          # 从串口接收指定字节数的数据
        ser.close()
        temp_data = data.decode()                   # 将接收到的数据解码并返回
        retValue = extract_first_float(temp_data)   # 提取字符串中的浮点数
        print(f"receive:{retValue}\n") #test
        if retValue != None:
            send_data_to_serial_port(serial_port,"1")
            print("success")#test
        return retValue
    
    except serial.SerialException as e:
        print(f"Error while receiving data from serial port: {str(e)}")
        return None
    
class EnhancedBinaryClassifier(nn.Module):
    def __init__(self):
        super(EnhancedBinaryClassifier, self).__init__()
        self.fc1 = nn.Linear(3, 128)  # 输入特征有3个
        self.dropout1 = nn.Dropout(0.5)
        self.fc2 = nn.Linear(128, 64)
        self.dropout2 = nn.Dropout(0.5)
        self.fc3 = nn.Linear(64, 1)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = self.dropout1(x)
        x = torch.relu(self.fc2(x))
        x = self.dropout2(x)
        x = torch.sigmoid(self.fc3(x))
        return x
    
print("getBMI\n") #test

extracted_data = None
# 打开文件并读取BMI数据
with open("/BMIdata.txt", "r") as file:
    for line in file:
        # 寻找以 # 开头，以 ; 结尾的数据
        if line.startswith("#") and line.endswith(";"):
            # 提取 # 和 ; 之间的数据
            start_index = line.find("#") + 1
            end_index = line.find(";")
            extracted_data = line[start_index:end_index]

if extracted_data == None:# 新增BMI输入
    temp_input = receive_data_from_serial_port(BLE_Slave_port,UART_MAX_SIZE)
    if temp_input != None:
        bmi_input = temp_input
        data = f"#{bmi_input};\n"
        with open("/BMIdata.txt", "wb") as file:
            file.write(data.encode("utf-8"))
            
print("getBMI:YES\n") #test

# 加载PyTorch模型的权重
model = EnhancedBinaryClassifier()  # 你需要定义YourModelClass
model.load_state_dict(torch.load("model_state_dict.pth"))
model.eval()

# 特征的均值和标准差，现在包括BMI
mean = [25.78961215, 667.56479135, 21.99759259]  # 假设BMI的均值为22.5
std = [15.09252548, 51.0466155, 4.40498747]  # 假设BMI的标准差为4.5

#System loop
while True:
    # 接收检测器的数据
    temp_input = receive_data_from_serial_port(BLE_Master_port,UART_MAX_SIZE)
    if temp_input == None or temp_input < 300:
        continue
    
    resistance_input = temp_input
    if oldResistance == 0 or resistance_input-oldResistance < 10:
        oldResistance = resistance_input

        # 获取当前时间值
        now_timetamp = time.time()
        time_input = float((now_timetamp-start_timestamp)/60)
    else:
        time_input = 0
        start_timestamp = time.time()

    # 使用训练时的均值和标准差对输入进行标准化
    time_normalized = (time_input - mean[0]) / std[0]
    resistance_normalized = (resistance_input - mean[1]) / std[1]
    bmi_normalized = (bmi_input - mean[2]) / std[2]  # BMI标准化

    # 将标准化后的数据转换为适合PyTorch的格式，现在包括三个特征
    input_data = torch.tensor([[time_normalized, resistance_normalized, 
                                bmi_normalized]], dtype=torch.float32)

    # 使用模型进行预测
    with torch.no_grad():
        predictions = model(input_data)

    # 输出所有预测结果
    # print(f"预测结果：{predictions.numpy()}")

    # 输出预测结果，假设阈值为0.5
    prediction = predictions.numpy()[0][0] > 0.5
    if prediction:
        state = 1
    else:
        state  =0
    send_data_to_serial_port(NET_port, '{'+f'"value": {resistance_input},\
"state": {state}'+'}')
    
    print('{'+f'"value":{resistance_input},\
"state":{state}'+'}')#test
