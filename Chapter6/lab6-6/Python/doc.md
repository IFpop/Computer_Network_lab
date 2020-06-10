# lab 6-6 Python



## 实验方法

使用 configparser 对于配置文件进行解析，获取相应的值存放于变量中。

由于实验要求为接受端仅做接收，故发送端模拟超时重传与3次重复应答的情况。

实现如下拥塞控制算法：

- 慢启动：定义拥塞窗口，一开始将该窗口大小设为1，之后每次收到确认应答（经过一个rtt），将拥塞窗口大小*2。此处使用 flag 变量决定经过一轮后对 conWindow 进行何种运算。

- 拥塞避免：设置慢启动阈值，一般开始都设为65536。拥塞避免是指当拥塞窗口大小达到这个阈值，拥塞窗口的值不再指数上升，而是加法增加（每次确认应答/每个rtt，拥塞窗口大小+1），以此来避免拥塞。

```python
if conWindow >= threshold:
                flag = '+1'
```

- 超时重传：将报文段的超时重传看做拥塞，则一旦发生超时重传，我们需要先将阈值设为当前窗口大小的一半，并且将窗口大小设为初值1，然后重新进入慢启动过程。

```python
if round % timeoutRound == 0:
                print(" Time out ")
                threshold = conWindow / 2
                conWindow = 1
                flag = '*2'
```

- 快速重传：在遇到3次重复确认应答（高速重发控制）时，代表收到了3个报文段，但是这之前的1个段丢失了，便对它进行立即重传。然后，先将阈值设为当前窗口大小的一半，然后将拥塞窗口大小设为慢启动阈值+3的大小。

```python
if round % triACKRound == 0:
                print(" Triple ACK ")
                round -= 1
                print("sending: " + str(round))
                print("round: " + str(round))
                print("window size" + str(conWindow))
                s.send(bytes(str(round), encoding='utf-8'))
                round += 1
                threshold = conWindow / 2
                conWindow = threshold + 3
                continue
```





## 实验结果

![output](.\output.png)