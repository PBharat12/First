'program to remove duplicate characters from the given input string' 
s=input("Enter The String:")
l=[]
for i in s:
    if i not in l:
        l.append(i)
res=''.join(l)
print(res)
