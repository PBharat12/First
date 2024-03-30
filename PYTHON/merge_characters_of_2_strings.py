'Program to merge characters of 2 strings into  a single string by taking characters alternatively.'
s1=input("Enter First String:")
s2=input("Enter Second String:")
i,j=0,0
res=''
while i<len(s1) or j<len(s2):
    if i<len(s1):
        res=res+s1[i]
        i+=1
    if j<len(s2):
        res=res+s2[j]
        j+=1
print(res)    
          