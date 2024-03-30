'Program to reverse internal content of each word.'
s=input("enter a string:")
l=s.split()
l1=[]
for i in l:
    l1.append(i[::-1])
r=' '.join(l1)
print(r)
