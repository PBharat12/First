#include<stdio.h>
int main(){
	int n,c=0,i;
	printf("ENTER A NUMBER:");
	scanf("%d",&n);
	for(i=1;i<=n ;i++)
	{
		if(n%i==0)
		{
			c+=1;
		}
	}
	if(c==2)
	{
		printf("NUMBER IS PRIME.");
	}
	else
	{
		printf("NUMBER IS NOT PRIME.");
	}
	return 0;
}
