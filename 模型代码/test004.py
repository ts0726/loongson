import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import matplotlib.pyplot as plt


df = pd.read_csv('Default Dataset.csv')


X = df[['time', 'R', 'BMI']].values
y = df['state'].values


scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)


print(f'Mean: {scaler.mean_}')
print(f'Standard deviation: {scaler.scale_}')


X_tensor = torch.tensor(X_scaled, dtype=torch.float32)
y_tensor = torch.tensor(y, dtype=torch.float32)


X_train, X_test, y_train, y_test = train_test_split(X_tensor, y_tensor, test_size=0.2, random_state=42)


train_data = TensorDataset(X_train, y_train)
test_data = TensorDataset(X_test, y_test)
train_loader = DataLoader(train_data, batch_size=64, shuffle=True)
test_loader = DataLoader(test_data, batch_size=64, shuffle=False)


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

model = EnhancedBinaryClassifier()


criterion = nn.BCELoss()
optimizer = optim.Adam(model.parameters(), lr=0.0005)

num_epochs = 100
for epoch in range(num_epochs):
    for inputs, labels in train_loader:
        optimizer.zero_grad()
        outputs = model(inputs)
        loss = criterion(outputs.squeeze(), labels)
        loss.backward()
        optimizer.step()


model.eval()
with torch.no_grad():
    correct = 0
    total = 0
    for inputs, labels in test_loader:
        outputs = model(inputs)
        predicted = outputs.squeeze() > 0.45
        total += labels.size(0)
        correct += (predicted == labels.byte()).sum().item()

accuracy = 100 * correct / total
print(f'Accuracy: {accuracy}%')


torch.save(model.state_dict(), 'model_state_dict.pth')
print("模型状态字典已保存为 model_state_dict.pth")


plt.scatter(df['time'], df['R'], c=df['state'], cmap='coolwarm')
plt.xlabel('time')
plt.ylabel('R')
plt.show()