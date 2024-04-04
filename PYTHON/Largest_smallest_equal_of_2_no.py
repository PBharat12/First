'''a program which prints small/large/equal relation of given two integers a and b.'''
a,b= map(int, input().split())
if(a<b):
    print("a < b")
elif(a>b):
        print("a > b")
else:
    print("a == b")
