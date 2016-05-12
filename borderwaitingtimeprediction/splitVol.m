function [ vol1,vol2,vol3,vol4,vol5,vol6,vol7,vol8,vol9,vol10,vol11,vol12] = splitVol( Vol )
% study/queueingmodel/random number production
% Vol
gap=0;
t=0;
b=3600/Vol;
vol1=0;
vol2=0;
vol3=0;
vol4=0;
vol5=0;
vol6=0;
vol7=0;
vol8=0;
vol9=0;
vol10=0;
vol11=0;
vol12=0;
while t<3600
    y=rand(1,1);
    gap=-log(1-y)*b;
    t=t+round(gap);
    if t<=300
        vol1=vol1+1;
    elseif t>300 && t<=600
        vol2=vol2+1;
    elseif t>600 && t<=900
        vol3=vol3+1;
    elseif t>900 && t<=1200
        vol4=vol4+1;
	elseif t>1200 && t<=1500
	vol5=vol5+1;
	elseif t>1500 && t<=1800
	vol6=vol6+1;
	elseif t>1800 && t<=2100
	vol7=vol7+1;
	elseif t>2100 && t<=2400
	vol8=vol8+1;
	elseif t>2400 && t<=2700
	vol9=vol9+1;
	elseif t>2700 && t<=3000
	vol10=vol10+1;
	elseif t>3000 && t<=3300
	vol11=vol11+1;
	elseif t>3300 && t<=3600
	vol12=vol12+1;
    end
end
vol1=vol1*12;
vol2=vol2*12;
vol3=vol3*12;
vol4=vol4*12;
vol5=vol5*12;
vol6=vol6*12;
vol7=vol7*12;
vol8=vol8*12;
vol9=vol9*12;
vol10=vol10*12;
vol11=vol11*12;
vol12=vol12*12;
end

