s=input("enter a string:")
print("the character which are at odd positions are:")
for i in range(0,len(s),2):
        print(s[i])
print("the character which are at even positions are:")
for i in range(1,len(s),2):
        print(s[i])
