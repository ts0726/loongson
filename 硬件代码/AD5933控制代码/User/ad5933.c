#include "sys.h"
#include "ad5933.h"
#include "delay.h"
#include <math.h>
#include "oled.h"

#define AD5933_MCLK	16.776e6 	//(Hz) AD5933�ڲ�Ƶ��
#define AD5933_SFreq	50000		//(Hz) V_OUT���Ƶ��

#define AD5933_ADDRESS	0x1A				//����AD5933�ӻ���ַ����Ĭ�ϴ������ߵ�ַΪ0001101(0x0D)
																		//�����ڴӻ���ַ����λΪ������ַ(0x0D)����ʵ�ʴӻ���ַΪ������ַ����һλ(0x1A)

#define	POINTER_COMMAND	0xB0				//����ָ����������(10110000)

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

int Receive_byte[1];								//�����������
short int status_register; 				  //����״̬�Ĵ�������

int Re, Im;//ʵ��,�鲿


void I2C_Configuration(void)
{
	
		I2C_InitTypeDef  I2C_InitStructure;
		GPIO_InitTypeDef  GPIO_InitStructure; 

		/* ʹ��I2C1��GPIOBʱ�� */
		RCC_APB1PeriphClockCmd(RCC_APB1Periph_I2C1,ENABLE);
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB,ENABLE);

		/* STM32F103C8T6оƬ��Ӳ��I2C: PB6 -- SCL; PB7 -- SDA */
		GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_6 | GPIO_Pin_7;
		GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
		GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_OD;				//I2C���뿪©���
		GPIO_Init(GPIOB, &GPIO_InitStructure);

		I2C_DeInit(I2C1);				//����I2C1

		//I2CӲ��������ʼ��
		I2C_InitStructure.I2C_Mode = I2C_Mode_I2C;
		I2C_InitStructure.I2C_DutyCycle = I2C_DutyCycle_2;
		I2C_InitStructure.I2C_OwnAddress1 = 0x30;				//������I2C��ַ,ֻҪ����ӻ���ַ��ͬ����
		I2C_InitStructure.I2C_Ack = I2C_Ack_Enable;			//Ӧ��ʹ��
		I2C_InitStructure.I2C_AcknowledgedAddress = I2C_AcknowledgedAddress_7bit;				//���ôӻ���ַΪ7λ
		I2C_InitStructure.I2C_ClockSpeed = 400000;			//I2Cʱ���ٶ�,400K
		I2C_Cmd(I2C1, ENABLE);
		//I2C_ITConfig(I2C1, I2C_IT_EVT | I2C_IT_BUF, ENABLE);			//����I2C�ж�(���ʹ���жϽ��յĻ�)
		I2C_Init(I2C1, &I2C_InitStructure);
		
}

void AD5933_Init(void)
{
		
		delay_ms(10);				//�������ʱ����Ҫ
	
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
		// place the AD5933 in standby mode����ģʽ
		i2c_write ( 0x80, 0xB0);

		// Choose the internal system clock
		i2c_write ( 0x81, 0x00);

		//��ʼ��
		i2c_write ( 0x80, 0x17);
		
		delay_ms(5);//�������ʱ��Ϊ��ʹ��·�ڷ�����ʼ�������ﵽ�ȶ�״̬
		
		//����
		i2c_write ( 0x80, 0x27);
		delay_ms(5);
}

void i2c_write( unsigned int reg_address, unsigned int reg_write_data)
{

		while(I2C_GetFlagStatus(I2C1, I2C_FLAG_BUSY));		//���I2C1�����Ƿ�ռ��
		
		I2C_GenerateSTART(I2C1, ENABLE);			//������ʼλ,������I2C1�����ϲ���һ����ʼ����(SCL�ߵ�ƽ,SDA�ɸߵ���)
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_MODE_SELECT));		//���EV5--��������Ϊ��ģʽ,����ʼ�����ѷ���

		I2C_Send7bitAddress(I2C1, AD5933_ADDRESS, I2C_Direction_Transmitter);				//����7λ�ӻ���ַ -- AD5933_ADDRESS = 0x1A
		while(!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_TRANSMITTER_MODE_SELECTED));		//���EV6--�ӻ�ȷ�ϵ�ַƥ�䲢Ӧ����������Ϊ������

		I2C_SendData(I2C1, reg_address);			//���ͼĴ�����ַ
		while (!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_BYTE_TRANSMITTED));			

		I2C_SendData(I2C1, reg_write_data);		//��������
		while (!I2C_CheckEvent(I2C1, I2C_EVENT_MASTER_BYTE_TRANSMITTED));			//���EV8_2--�ֽڷ������,��������ֹͣλ			
		
		I2C_GenerateSTOP(I2C1, ENABLE);				//����ֹͣλ,������I2C1�����ϲ���һ��ֹͣ����(SCL�ߵ�ƽ,SDA�ɵ͵���)
	  
}
	 
int AD5933_read (unsigned int register_address)
{

   i2c_write(POINTER_COMMAND, register_address);		// ���üĴ�����ַָ�����ڶ�ȡ�Ĵ�������,POINTER_COMMANDΪ����ָ������--10110000(0xB0)
   i2c_read();								//��ָ���ļĴ����ж�ȡ����
   return(Receive_byte[0]);		//���ر�����Receive_byte����������ȡ��������

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
		I2C_AcknowledgeConfig(I2C1, DISABLE);				//����Ӧ����,�������յ����һ���ֽ�ʱ��Ӧ���ʾ���ս���
	
		/* Send STOP Condition */
		I2C_GenerateSTOP(I2C1, ENABLE);

    /* Wait for the byte to be received */
    while(I2C_GetFlagStatus(I2C1, I2C_FLAG_RXNE) == RESET);				//��������Ƿ��յ�������
		
		/* Read a byte from the AD5933 */
		Receive_byte[0] = I2C_ReceiveData(I2C1);				//�����յ����ݺ����ݴ���DR�Ĵ�������ʱ��ȡ�üĴ������ݲ����浽����������
		
    /* Re-Enable Acknowledgement to be ready for another reception */
    I2C_AcknowledgeConfig(I2C1, ENABLE); 				//����Ӧ����,Ϊ��һ�ν�����׼��
		
}


double sweep (void)
{
	double Magnitude, Impedance;//DFT����ֵ,�迹ֵ
	
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
			Magnitude = sqrt(pow(Re,2) + pow(Im,2));//����DFT����ֵ
			
			//��Ϊ�������Ͻ���
			//Impedance = 1/(Magnitude * GainFactor);//����Ҷ�����迹
			
			//test*******************
			//Impedance = Magnitude;
			
				
			i2c_write ( 0x80, 0x31);			// increment to the next frequency
				
			// place the AD5933 in standby mode����ģʽ
			i2c_write ( 0x80, 0xB0);
			return Magnitude;
		}
	}
	return 0;
}// end of sweep function
