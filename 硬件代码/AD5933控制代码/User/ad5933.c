#include "sys.h"
#include "ad5933.h"
#include "delay.h"
#include <math.h>
#include "oled.h"

#define AD5933_MCLK	16.776e6 	//(Hz) AD5933内部频率
#define AD5933_SFreq	50000		//(Hz) V_OUT输出频率

#define AD5933_ADDRESS	0x1A				//定义AD5933从机地址，其默认串行总线地址为0001101(0x0D)
																		//但由于从机地址高七位为器件地址(0x0D)，故实际从机地址为器件地址左移一位(0x1A)

#define	POINTER_COMMAND	0xB0				//定义指针设置命令(10110000)

#define Gain_factor 5.0725e-9
#define PI 3.1415926536
#define System_Phase 109.052767
#define rad_to_degree 180 / PI

unsigned char tab1[16]={"Impedance:"};
unsigned char tab2[16]={"0000.0000 Kohm"};
unsigned char tab3[16]={"Phase:"};
unsigned char tab4[16]={"000  Degree"};
int inter,temp;

double GainFactor = 5.1143e-9;//, 5.1146e-9, 5.1162e-9, 5.11645e-9, 5.11699e-9, 5.1175e-9, 5.11774e-9, 5.1189e-9, 5.11983e-9, 5.12105e-9};
//double SystemPhase = 108.647;//, 109.279, 109.847, 110.485, 111.151, 111.763, 112.361, 112.953, 113.608, 114.225};

int Receive_byte[1];								//定义接收数组
short int status_register; 				  //定义状态寄存器变量

int Re, Im;//实部,虚部


void I2C_Configuration(void)
{
	
		I2C_InitTypeDef  I2C_InitStructure;
		GPIO_InitTypeDef  GPIO_InitStructure; 

		/* 使能I2C1和GPIOB时钟 */
		RCC_APB1PeriphClockCmd(RCC_APB1Periph_I2C1,ENABLE);
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB,ENABLE);

		/* STM32F103C8T6芯片的硬件I2C: PB6 -- SCL; PB7 -- SDA */
		GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_6 | GPIO_Pin_7;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_OD;				//I2C必须开漏输出
		GPIO_Init(GPIOB, &GPIO_InitStructure);

		I2C_DeInit(I2C1);				//重置I2C1

		//I2C硬件参数初始化
		I2C_InitStructure.I2C_Mode = I2C_Mode_I2C;
		I2C_InitStructure.I2C_DutyCycle = I2C_DutyCycle_2;
		I2C_InitStructure.I2C_OwnAddress1 = 0x30;				//主机的I2C地址,只要不与从机地址相同即可
		I2C_InitStructure.I2C_Ack = I2C_Ack_Enable;			//应答使能
		I2C_InitStructure.I2C_AcknowledgedAddress = I2C_AcknowledgedAddress_7bit;				//配置从机地址为7位
		I2C_InitStructure.I2C_ClockSpeed = 400000;			//I2C时钟速度,400K
		I2C_Cmd(I2C1, ENABLE);
		//I2C_ITConfig(I2C1, I2C_IT_EVT | I2C_IT_BUF, ENABLE);			//启用I2C中断(如果使用中断接收的话)
		I2C_Init(I2C1, &I2C_InitStructure);
		
}

void AD5933_Init(void)
{
		
		delay_ms(10);				//这里的延时很重要
	
		// Transmit	to start frequency register 
		// program 50khz start frequency assuming internal osc of 16.776Khz
		i2c_write ( 0x84, 0xFA);
		i2c_write ( 0x83, 0x70); 
		i2c_write ( 0x82, 0x02);

		// Transmit to frequency increment register 
		// program 1Khz frequency increment assuming internal osc of 16.776Khz
		i2c_write ( 0x87, 0x00);
		i2c_write ( 0x86, 0x00);    
		i2c_write ( 0x85, 0x00);

		// Transmit to NUMBER OF INCREMENTS register 
		// program 10 frequency increments
		i2c_write ( 0x89, 0xFF);
		i2c_write ( 0x88, 0x00);

		// Transmit to settling time cycles register 
		// program 15 output cycles at each frequency before a adc conversion
		i2c_write ( 0x8B, 0x0F);
		i2c_write ( 0x8A, 0x00);

		// Transmit to CONTROL register 
		// place the AD5933 in standby mode待机模式
		i2c_write ( 0x80, 0xB0);

		// Choose the internal system clock
		i2c_write ( 0x81, 0x00);

		//初始化
		i2c_write ( 0x80, 0x17);
		
		delay_ms(5);//这里的延时是为了使电路在发出初始化命令后达到稳定状态
		
		//启动
		i2c_write ( 0x80, 0x27);
		delay_ms(5);
}

void i2c_write( unsigned int reg_address, unsigned int reg_write_data)
{

		while(I2C_GetFlagStatus(I2C1, I2C_FLAG_BUSY));		//检查I2C1总线是否被占用
		
		I2C_GenerateSTART(I2C1, ENABLE);			//设置起始位,主机在I2C1总线上产生一个起始条件(SCL高电平,SDA由高到低)
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_MODE_SELECT));		//检查EV5--主机设置为主模式,且起始条件已发送

		I2C_Send7bitAddress(I2C1, AD5933_ADDRESS, I2C_Direction_Transmitter);				//发送7位从机地址 -- AD5933_ADDRESS = 0x1A
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_TRANSMITTER_MODE_SELECTED));		//检查EV6--从机确认地址匹配并应答，主机设置为发送器

		I2C_SendData(I2C1, reg_address);			//发送寄存器地址
		while (!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_BYTE_TRANSMITTED));			

		I2C_SendData(I2C1, reg_write_data);		//发送数据
		while (!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_BYTE_TRANSMITTED));			//检测EV8_2--字节发送完成,请求设置停止位			
		
		I2C_GenerateSTOP(I2C1, ENABLE);				//设置停止位,主机在I2C1总线上产生一个停止条件(SCL高电平,SDA由低到高)
	  
}
	 
int AD5933_read (unsigned int register_address)
{

   i2c_write(POINTER_COMMAND, register_address);		// 设置寄存器地址指针用于读取寄存器内容,POINTER_COMMAND为设置指针命令--10110000(0xB0)
   i2c_read();								//从指定的寄存器中读取内容
   return(Receive_byte[0]);		//返回保存在Receive_byte数组中所读取到的内容

}
	 
void i2c_read(void)
{
		   
		I2C_GenerateSTART(I2C1, ENABLE);
		
		/* Test on EV5 and clear it */
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_MODE_SELECT));
		
		/* Send AD5933 address for read */
		I2C_Send7bitAddress(I2C1, AD5933_ADDRESS, I2C_Direction_Receiver);
		
		/* Test on EV6 and clear it */
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_RECEIVER_MODE_SELECTED));
		
		/* Disable Acknowledgement */
		I2C_AcknowledgeConfig(I2C1, DISABLE);				//禁用应答功能,当主机收到最后一个字节时不应答表示接收结束
	
		/* Send STOP Condition */
		I2C_GenerateSTOP(I2C1, ENABLE);

    /* Wait for the byte to be received */
    while(I2C_GetFlagStatus(I2C1, I2C_FLAG_RXNE) == RESET);				//检查主机是否收到了数据
		
		/* Read a byte from the AD5933 */
		Receive_byte[0] = I2C_ReceiveData(I2C1);				//主机收到数据后将数据存于DR寄存器，此时读取该寄存器内容并保存到接收数组中
		
    /* Re-Enable Acknowledgement to be ready for another reception */
    I2C_AcknowledgeConfig(I2C1, ENABLE); 				//启用应答功能,为下一次接收做准备
		
}


double sweep (void)
{
	double Magnitude, Impedance;//DFT幅度值,阻抗值
	
	unsigned int real_byte_high;
	unsigned int real_byte_low;

	unsigned int imag_byte_high;
	unsigned int imag_byte_low;

	signed short int imag_data;
	signed short int real_data;

	delay_ms(5);

	// D1 status reg loop
	status_register = AD5933_read(0x8F);     		// read the status register
	status_register = (status_register & 0x2);  // mask off the valid data bit
			
	if( ((status_register)| 0xFD )==  0xFF)  		// valid data should be present after start freqy command
	{
	//D1 true condition
	//printf ("Status register is %u (dec) \n",status_register);       // printf function call

		if( (AD5933_read(0x8F)| 0xFB )!=  0xFF)	// D2 test condition
		{
			real_byte_high = AD5933_read(0x94);
			real_byte_low =  AD5933_read(0x95);
			imag_byte_high = AD5933_read(0x96);
			imag_byte_low =  AD5933_read(0x97);

			real_data = ((real_byte_high << 8) |  real_byte_low);
				
			imag_data = ((imag_byte_high << 8) |  imag_byte_low);
				
			Re = (int) real_data;
			Im = (int) imag_data;
			Magnitude = sqrt(pow(Re,2) + pow(Im,2));//测量DFT幅度值
			
			//改为在主机上进行
			//Impedance = 1/(Magnitude * GainFactor);//傅里叶计算阻抗
			
			//test*******************
			//Impedance = Magnitude;
			
				
			i2c_write ( 0x80, 0x31);			// increment to the next frequency
				
			// place the AD5933 in standby mode待机模式
			i2c_write ( 0x80, 0xB0);
			return Magnitude;
		}
	}
	return 0;
}// end of sweep function
