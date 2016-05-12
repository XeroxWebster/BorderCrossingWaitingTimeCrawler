function [waitingtime,numV,nLane_new,cost] = queueingmodel(Vol,lastVol,nLane_old,threshold)

% n1=0; % line 12,13 numBooth % line 
n11=nLane_old;
% Cost_fi=[];
% num_veh=[];
% N_fi=[];
% numV_previoushour=0;

Vehleft=[];% left vehicle at the end of 20
Cost=[]; %cost of each situation
Delay=[];
numLanes=3:10; % number of open lanes
%threshold=40; % threshold=40 minutes
for numBooth=3:10 % minimum number 3, maximum 10
    % numBooth=2;
    Occupacy=1000;
    if numBooth==3
        Occupacy=260+3;
    elseif numBooth==4
        Occupacy=260+18+4;
    elseif numBooth==5
        Occupacy=260+18+15+5;
    elseif numBooth==6
        Occupacy=260+18+15+12+6;
    end
    Ek=2; % order of Erlang distribution
    
    % k=1; % maximum number of vehicles in system
    T=1; % total time in system
    State=0; % total states in system
    
    arrR=Vol/3600; % arrival rate 600 is the 400 in the paper.
    
    % arrR=1/(1/arrR+6); % 6 here is to simulate the arrival process in VISSIM
    arrR=1/(1/arrR+0);
    arrR1=arrR;
    serR=1/44.58; % service rate this one should also be changed?
    PROB=1;%the prob of 0 state and 0 vehicle
    numV=lastVol; % the left volume of last time interval
    V=[];
    Vevery=[];
    V_max=[];
    ARR=[];
    SER=[];
    
    
    true=0;
    while T<=300 % update every 5 mins
        %average
        if numV<=numBooth
            X=zeros((numV+2)*(numV+1)/2,2);
            for i=1:1:numV
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
        elseif numV>numBooth && numV<Occupacy
            X=zeros((numBooth+2)*(numBooth+1)/2+(numV-numBooth)*(numBooth+1),2);
            for i=1:1:numBooth
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
            for i=numBooth+1:numV
                for j=(numBooth+2)*(numBooth+1)/2+(i-1-numBooth)*(numBooth+1)+1:1:(numBooth+2)*(numBooth+1)/2+(i-numBooth)*(numBooth+1)
                    X(j,1)=X(j-(numBooth+1),1)+Ek;
                    X(j,2)=i;
                end
            end
        elseif numV>=Occupacy
            X=zeros((numBooth+2)*(numBooth+1)/2+(Occupacy-numBooth)*(numBooth+1),2);
            for i=1:1:numBooth
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
            for i=numBooth+1:Occupacy
                for j=(numBooth+2)*(numBooth+1)/2+(i-1-numBooth)*(numBooth+1)+1:1:(numBooth+2)*(numBooth+1)/2+(i-numBooth)*(numBooth+1)
                    X(j,1)=X(j-(numBooth+1),1)+Ek;
                    X(j,2)=i;
                end
            end
        end
        
        gap=1/(arrR1);
        arrR=0;
        ARR=[ARR;arrR];
        %
        for t1=1:1:round(gap)
            serR=2/117.16;
            [e,f]=size(X);
            Prob_new=zeros(e,1);
            Prob_old=zeros(e,1);
            
            if numV~=0 && true==0
                Prob_old(e)=1;
                true=1;
            else
                [g,h]=size(PROB);
                Prob_old(1:g,:)=PROB; % 上一个时间步echo的各个states可能的概率，用来计算当前时间步的概率
            end
            
            if e>1
                for i=e:-1:1
                    if X(i,1)==0 && X(i,2)==0
                        % equation 18
                        Prob_new(1)=Prob_old(1)-arrR *Prob_old(1)+Ek*serR *Prob_old(2);
                        if Prob_new(1)<0
                            Prob_new(1)=0;
                        end
                    else
                        a=find(X(:,2)==X(i,2)); %find the num of the rank X(i,1),X(i,2)
                        [n1,n2]=size(a);
                        c=find(X(a(1):a(n1),1)==X(i,1)); %the order of the state in the states with the same # of vehicles
                        if c==1 && X(i,2)<numBooth
                            Prob_less=(Ek*X(i,2)-X(i,2)-1)/X(i,2);
                            Prob_same=1-Prob_less;
                            b=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b);
                            if n3~=0
                                d=find(X(b(1):b(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,2)-1)/(X(i,2)+1);
                                %                 Prob(a(c),b(d))=Prob_less*(X(i,2)+1)*Ek*serR ;
                                %euqation 21
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b(d));
                                if Prob_new(a(c))<0
                                    Prob_new(a(c))=0;
                                end
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                                if Prob_new(a(c))<0
                                    Prob_new(a(c))=0;
                                end
                            end
                        elseif c==1 && X(i,2)==numBooth
                            Prob_less=(Ek*X(i,2)-X(i,2)-1)/X(i,2);
                            Prob_same=1-Prob_less;
                            % equation 24
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c==1 && X(i,2)>numBooth && X(i,2)<Occupacy
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d=find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            Prob_less=(Ek*numBooth-numBooth-1)/numBooth;
                            Prob_same=1-Prob_less;
                            % equation 27
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c~=n1 && X(i,2)==Occupacy
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d=find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            Prob_less=(Ek*numBooth-numBooth-1)/numBooth;
                            Prob_same=1-Prob_less;
                            % equation 29
                            Prob_new(a(c))=Prob_old(a(c))-(numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c==n1 && X(i,2)<numBooth
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/(X(i,2)+1);
                                % equation 19
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c==n1 && X(i,2)==numBooth
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                                %euqation 22
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c==n1 && X(i,2)>numBooth && X(i,2)<Occupacy
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                                %equation 25
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c==n1 && X(i,2)==Occupacy
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                            %                     equation 28
                            Prob_new(a(c))=Prob_old(a(c))-(numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c>1 && c<n1 && X(i,2)<numBooth
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                            Prob_less=(Ek*X(i,2)-X(i,1)-1)/X(i,2);
                            Prob_same=1-Prob_less;
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/(X(i,2)+1);
                                % equation 20
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c>1 && c<n1 && X(i,2)==numBooth
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                            Prob_less=(Ek*X(i,2)-X(i,1)-1)/X(i,2);
                            Prob_same=1-Prob_less;
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                                % equation 23
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif c>1 && c<n1 && X(i,2)>numBooth && X(i,2)<Occupacy
                            b=find(X(:,2)==X(i,2)-1);
                            [n3,n4]=size(b);
                            d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                            Prob_less=(Ek*X(i,2)-X(i,1)-1)/numBooth;
                            Prob_same=1-Prob_less;
                            b1=find(X(:,2)==X(i,2)+1);
                            [n3,n4]=size(b1);
                            if n3~=0
                                d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                                Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                                %euqation 26
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1)+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                            elseif n3==0
                                Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                            end
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        end
                    end
                end
            elseif e==1
                Prob_new=Prob_old;
            end
            PROB=Prob_new;
            SUM=sum(Prob_new);
            [a,b]=size(Prob_new);
            for i=1:1:a
                Prob_new(i)=Prob_new(i)/SUM;
            end     
        end
        
        
        N=(numBooth+2)*(numBooth+1)/2; % the state number with numBooth servers
        [a,b]=size(Prob_new);
        P=[];
        if numV>0
            if numV<=numBooth
                for i=1:1:numV+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
            elseif numV>numBooth && numV<Occupacy
                for i=1:1:numBooth+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
                for i=numBooth+1:1:numV
                    p=sum(Prob_new(N+(numBooth+1)*(i-1-numBooth)+1:N+(numBooth+1)*(i-numBooth)));
                    P=[P;p];
                end
            elseif numV>=Occupacy
                for i=1:1:numBooth+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
                for i=numBooth+1:1:Occupacy
                    p=sum(Prob_new(N+(numBooth+1)*(i-1-numBooth)+1:N+(numBooth+1)*(i-numBooth)));
                    P=[P;p];
                end
            end
            Vehicle1=0;
            [b4,b5]=max(P);
            Vehicle_max1=b5;
            if numV<Occupacy
                for i=0:1:numV % expect number of vehicles
                    Vehicle1=Vehicle1+i*P(i+1);
                end
            elseif numV>=Occupacy
                for i=0:1:Occupacy % expect number of vehicles
                    Vehicle1=Vehicle1+i*P(i+1);
                end
            end
            V=[V;Vehicle1]; % record the expect number of vehicles every time
            V_max=[V_max;Vehicle_max1];
        end

        arrR=arrR1*gap;
        ARR=[ARR;arrR];
        serR=0;
        % this step, the vehicle join the queue
        numV=numV+1;
        if numV<=numBooth
            X=zeros((numV+2)*(numV+1)/2,2);
            for i=1:1:numV
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
        elseif numV>numBooth && numV<Occupacy
            X=zeros((numBooth+2)*(numBooth+1)/2+(numV-numBooth)*(numBooth+1),2);
            for i=1:1:numBooth
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
            for i=numBooth+1:numV
                for j=(numBooth+2)*(numBooth+1)/2+(i-1-numBooth)*(numBooth+1)+1:1:(numBooth+2)*(numBooth+1)/2+(i-numBooth)*(numBooth+1)
                    X(j,1)=X(j-(numBooth+1),1)+Ek;
                    X(j,2)=i;
                end
            end
        elseif numV>=Occupacy
            X=zeros((numBooth+2)*(numBooth+1)/2+(Occupacy-numBooth)*(numBooth+1),2);
            for i=1:1:numBooth
                for j=(i+1)*i/2+1:1:(i+2)*(i+1)/2 % the possible number of stages when k=2
                    X(j,1)=i+j-(i+1)*i/2-1; % the minimum stages are i
                    X(j,2)=i;
                end
            end
            for i=numBooth+1:Occupacy
                for j=(numBooth+2)*(numBooth+1)/2+(i-1-numBooth)*(numBooth+1)+1:1:(numBooth+2)*(numBooth+1)/2+(i-numBooth)*(numBooth+1)
                    X(j,1)=X(j-(numBooth+1),1)+Ek;
                    X(j,2)=i;
                end
            end
        end
        [e,f]=size(X);
        Prob_old=zeros(e,1);
        Prob_new=zeros(e,1);
        
        [g,h]=size(PROB);
        Prob_old(1:g,:)=PROB; % 上一个时间步echo的各个states可能的概率，用来计算当前时间步的概率
        if e>1
            for i=e:-1:1
                if X(i,1)==0 && X(i,2)==0
                    % equation 18
                    Prob_new(1)=Prob_old(1)-arrR *Prob_old(1)+Ek*serR *Prob_old(2);
                    if Prob_new(1)<0
                        Prob_new(1)=0;                   end
                else
                    a=find(X(:,2)==X(i,2)); %find the num of the rank X(i,1),X(i,2)
                    [n1,n2]=size(a);
                    c=find(X(a(1):a(n1),1)==X(i,1)); %the order of the state in the states with the same # of vehicles
                    if c==1 && X(i,2)<numBooth
                        Prob_less=(Ek*X(i,2)-X(i,2)-1)/X(i,2);
                        Prob_same=1-Prob_less;
                        b=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b);
                        if n3~=0
                            d=find(X(b(1):b(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,2)-1)/(X(i,2)+1);
                            %                 Prob(a(c),b(d))=Prob_less*(X(i,2)+1)*Ek*serR ;
                            %euqation 21
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b(d));
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                            if Prob_new(a(c))<0
                                Prob_new(a(c))=0;
                            end
                        end
                    elseif c==1 && X(i,2)==numBooth
                        Prob_less=(Ek*X(i,2)-X(i,2)-1)/X(i,2);
                        Prob_same=1-Prob_less;
                        % equation 24
                        Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c==1 && X(i,2)>numBooth && X(i,2)<Occupacy
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d=find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        Prob_less=(Ek*numBooth-numBooth-1)/numBooth;
                        Prob_same=1-Prob_less;
                        % equation 27
                        Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c~=n1 && X(i,2)==Occupacy
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d=find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        Prob_less=(Ek*numBooth-numBooth-1)/numBooth;
                        Prob_same=1-Prob_less;
                        % equation 29
                        Prob_new(a(c))=Prob_old(a(c))-(numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c==n1 && X(i,2)<numBooth
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/(X(i,2)+1);
                            % equation 19
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c==n1 && X(i,2)==numBooth
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                            %euqation 22
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c==n1 && X(i,2)>numBooth && X(i,2)<Occupacy
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                            %equation 25
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c==n1 && X(i,2)==Occupacy
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d= find(X(b(1):b(n3),1)==X(i,1)-Ek);
                        %                     equation 28
                        Prob_new(a(c))=Prob_old(a(c))-(numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d));
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c>1 && c<n1 && X(i,2)<numBooth
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                        Prob_less=(Ek*X(i,2)-X(i,1)-1)/X(i,2);
                        Prob_same=1-Prob_less;
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/(X(i,2)+1);
                            % equation 20
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*(X(i,2)+1)*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c>1 && c<n1 && X(i,2)==numBooth
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                        Prob_less=(Ek*X(i,2)-X(i,1)-1)/X(i,2);
                        Prob_same=1-Prob_less;
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                            % equation 23
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1)+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +X(i,2)*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*X(i,2)*Ek*serR *Prob_old(a(c)+1);
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    elseif c>1 && c<n1 && X(i,2)>numBooth && X(i,2)<Occupacy
                        b=find(X(:,2)==X(i,2)-1);
                        [n3,n4]=size(b);
                        d=find( X(b(1):b(n3),1)==X(i,1)-Ek);
                        Prob_less=(Ek*X(i,2)-X(i,1)-1)/numBooth;
                        Prob_same=1-Prob_less;
                        b1=find(X(:,2)==X(i,2)+1);
                        [n3,n4]=size(b1);
                        if n3~=0
                            d1=find(X(b1(1):b1(n3),1)==X(i,1)+1);
                            Prob_less=(Ek*(X(i,2)+1)-X(i,1)-1)/numBooth;
                            %euqation 26
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1)+Prob_less*numBooth*Ek*serR *Prob_old(b1(d1));
                        elseif n3==0
                            Prob_new(a(c))=Prob_old(a(c))-(arrR +numBooth*Ek*serR )*Prob_old(a(c))+arrR *Prob_old(b(d))+Prob_same*numBooth*Ek*serR *Prob_old(a(c)+1);
                        end
                        if Prob_new(a(c))<0
                            Prob_new(a(c))=0;
                        end
                    end
                end
            end
        elseif e==1
            Prob_new=Prob_old;
        end
        SUM=sum(Prob_new);
        [a,b]=size(Prob_new);
        for i=1:1:a
            Prob_new(i)=Prob_new(i)/SUM;
        end
        %Prob_new=Prob_new/sum(Prob_new);
        PROB=Prob_new;
        N=(numBooth+2)*(numBooth+1)/2;
        [a,b]=size(Prob_new);
        P=[];
        if numV>0
            if numV<=numBooth
                for i=1:1:numV+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
            elseif numV>numBooth && numV<Occupacy
                for i=1:1:numBooth+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
                for i=numBooth+1:1:numV
                    p=sum(Prob_new(N+(numBooth+1)*(i-1-numBooth)+1:N+(numBooth+1)*(i-numBooth)));
                    P=[P;p];
                end
            elseif numV>=Occupacy
                for i=1:1:numBooth+1
                    p=sum(Prob_new(sum(1:i-1)+1:sum(1:i)));
                    P=[P;p];
                end
                for i=numBooth+1:1:Occupacy
                    p=sum(Prob_new(N+(numBooth+1)*(i-1-numBooth)+1:N+(numBooth+1)*(i-numBooth)));
                    P=[P;p];
                end
            end
            Vehicle=0;
            [b4,b5]=max(P);
            Vehicle_max=b5;
            if numV<Occupacy
                for i=0:1:numV % expect number of vehicles
                    Vehicle=Vehicle+i*P(i+1);
                end
            elseif numV>=Occupacy
                for i=0:1:Occupacy % expect number of vehicles
                    Vehicle=Vehicle+i*P(i+1);
                end
            end
            V=[V;Vehicle]; % record the expect number of vehicles every time
            V_max=[V_max;Vehicle_max];
        end
        T=T+round(gap);% still driven by the # of vehicles;
    end
    Cop=200;%operate one booth
    Cw=25;%waiting cost
    Cp=20;%punishment
    De=Vehicle*44.58/numBooth/60;%contraints
    if numBooth>n11
        C=(numBooth*Cop*20/60+Cw*mean(V)*20/60)+Cp*(numBooth-n11);%cost
    else
        C=(numBooth*Cop*20/60+Cw*mean(V)*20/60)+Cp*(n11-numBooth);
    end
    
    % De=Vehicle*44.58/numBooth/60;%contraints
    Vehleft=[Vehleft;Vehicle];% left vehicle at the end of 15 mins
    Cost=[Cost;C]; %cost of each situation
    Delay=[Delay;De];
    
end
[m1,n2]=sort(Cost);
[s1,s2]=size(Cost);
for k=1:1:s1
    if Delay(n2(k))<=threshold %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%delay cannot be larger than 30 mmins
        break;
    end
end
Ve_last=Vehleft;
numV=round(Ve_last(n2(k))); %the vehicle number in the queue in the previous hour
nLane_new=numLanes(n2(k));
waitingtime=Delay(n2(k));
cost=Cost(n2(k));
% num_veh=[num_veh;numV];
% Cost_fi=[Cost_fi;Cost(n2(k))];%cost of each period
% N_fi=[N_fi;n2(k)];%open lanes
end