#include "sys.h"
#include "usart.h"
#include "delay.h"
#include "ad5933.h"
#include <stdio.h>

#define ARR_SIZE	20	//�����С

int main(void)
{
	//DFT����ֵ����
  double Mdata = 0;
	double Msum = 0;
	double Maverage = 0;
	
	//�迹����
	double Idata = 0;
	double Isum = 0;
	double Iaverage = 0;
	
	uint8_t i=0;
	uint8_t t=0;
	
	char upLoadStr[20];
	
		//short int status_register ;
		delay_init();	    	 			//��ʱ������ʼ��	
		NVIC_Configuration();			//�����ж����ȼ�����
		uart_init(9600);	 				//���ڳ�ʼ��Ϊ9600

		I2C_Configuration();		  //����I2C�ӿڲ��� 
		AD5933_Init();						//AD5933��ʼ��
	
	
	while(1)
	{
		for(t=0;t<10;t++)
		{
			//test3.0����ƽ��������ֵ
			for(i=0;i<20;i++)
			{
				Mdata = sweep ();
				Msum += Mdata;
				AD5933_Init();
				
				Mdata = 0;
			}
			Maverage = Msum / ARR_SIZE;
			Idata = 1.0/(Maverage * 5.1143e-9*20);
			Isum += Idata;
			
			Msum = 0;
			Maverage = 0;
			Idata = 0;
		}
		Iaverage = Isum / 10;
		
		sprintf(upLoadStr,"#%f;\n",Iaverage);
		for(i=0;i<10;i++)
		{
			printf(upLoadStr);
		}
		
		for(i=0;i<150;i++)
		{
			delay_ms(60*1000);
		}
		
		//delay_ms(1000);//test-delay
		
		Isum = 0;
		Iaverage = 0;
	}
	
//			//test2.1������ֵ����ƽ��
//		for(i=0;i<20;i++)
//		{
//			Mdata = 0;
//			Mdata = sweep ();
//			Idata = 1.0/(Mdata * 5.1143e-9*20);
//			sum += Idata;
//			
//			AD5933_Init();
//		}
//				
//		average = sum / ARR_SIZE;
//		sum = 0;
//		
//		//sprintf(upLoadStr,"#%f;\n",average);
//		sprintf(upLoadStr,"%f,\n",average);//test
//		printf(upLoadStr);
//		delay_ms(1000);
//		///////////
		
	
//		//test2.0
//		double Magnitude=0;//DFT����ֵ
//
//		Magnitude = sweep ();
//		sprintf(upLoadStr,"%f,\n",Magnitude);
//		printf(upLoadStr);
//		Magnitude = 0;
//		delay_ms(1000);
//		AD5933_Init();
//		///////////
		
//		test1.0
//		Impedance = sweep ();									//����Ƶ��ɨ��
//		if(testTime>1&&testTime<maxTime)
//		{
//			sprintf(upLoadStr,"#%f;\n",Impedance);
//			printf(upLoadStr);
//			delay_ms(1000);//test
//			//delay_ms(60000);
//		}
//		else if(testTime == maxTime)
//		{
//			AD5933_Init();
//			testTime=0;
//		}
//		testTime++;
//		Impedance = 0;
//}
////////////////////
}