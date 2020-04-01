#coding=utf-8
#create: 2020/3/31

import random
import hashlib
import sys

def Md5_fun(Key,Random_num):
    md5 = hashlib.md5()
    # 生成md5加密的明文
    m = Random_num+Key
    # print(m)
    md5.update(m.encode('utf-8'))
    return md5.hexdigest()

if __name__ == "__main__":
    # 设置口令，口令是验证方所有
    Key = "abc123456efg"

    # 取具体6位的0~9之内的不重字的随机数，比较像随机验证码
    temp = random.sample(range(0,10), 6)
    Random_num = ''.join('%s' %id for id in temp)

    # 被验证方使用口令和随机数生成md5值
    Input_Key = input('请输入你的口令:')
    send_md5 = Md5_fun(Input_Key,Random_num)
    # print(send_md5)

    # 现在进行验证方操作
    # 口令在验证方都也有
    print("当前需要验证的口令: "+Input_Key)
    print("\n生成的随机数: "+Random_num)

    print("发送来的MD5摘要值: "+send_md5+'\n')

    # 使用验证方保存的Key进行MD操作
    recv_md5 = Md5_fun(Key,Random_num)
    print("验证方生成的MD5摘要值: "+recv_md5)

    if(recv_md5 == send_md5):
        print("验证成功")
    else:
        print("验证失败")

    