l=eval(input("Enter list : "))
for i in range(0,len(l)):
    for j in range(i+1,len(l)):
        t=0
        if l[i]>l[j]:
            t=l[i]
            l[i]=l[j]
            l[j]=t
print("the 2nd smallest element is:",l[1])
print("the 2nd largest element is:",l[len(l)-2])
