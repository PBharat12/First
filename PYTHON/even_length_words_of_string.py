n=input("enter a string:")
s=n.split(" ")
print("even length words in a string are:")
for i in s: 
  if len(i)%2==0: 
    print(i)
