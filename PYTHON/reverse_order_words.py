'Program to reverse order of words.'
s=input("Enter a string:")
l=s.split()
l1=[]
i=len(l)-1
while i!=-1:
    l1.append(l[i])
    i=i-1
r=' '.join(l1)
print(r)    
