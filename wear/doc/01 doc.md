#### Document 

#### IMU惯性测量单元

> /data/misc/sensor 
sensor data 

> adb pull /data/misc/sensor

1.5.1.19 (Dev)

> adb pull  /data/data/com.huami.watch.newsport/databases

#### 周五运动三次

> acc 有
> 地磁没有
> 陀螺仪的没有


adb shell  getprop sport_geomagnetic;

adb shell getprop sport_sensor_hub_data;
adb  pull /data/data/com.huami.watch.newsport/databases
adb  pull data/misc/sensor ;
