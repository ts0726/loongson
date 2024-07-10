#ifndef __AD5933_H
#define __AD5933_H

void I2C_Configuration(void);
void AD5933_Init(void);
void i2c_write ( unsigned int reg_address, unsigned int reg_write_data);
int  AD5933_read (unsigned int register_address);
void i2c_read(void);
double sweep (void);
void convert (short bin,unsigned char *str);
#endif
