int test(int a, int b) {
    print << a;
    ret b;
}

int arithmetic(){
    int a;
    int b;
    int c;
    int d;
    a = 1;
    b = 2;
    c = 3;
    d = a+b;
    print << d;
    print << " ";
    d = d-1;
    print << d;
    print << " ";
    d = d*0;
    print << d;
    print << " ";
    d = 1/1;
    print << d;
    print << " ";
    ret a;
}
void retVoid(){

}
bool logic(){
    bool a;
    bool b;
    bool c;
    bool d;
    a = tru;
    b = fls;
    c = !tru;
    d = !a; 
    print << d;
    print << " ";
    d = a && b;
    print << d;
    print << " ";
    d = a || b;
    print << d;
    print << " ";
    d = !(a||b); 
    print << d;
    print << " ";
    d = (1==1) && (0==0) && (!b);
    print << d;
    print << "\n";
    ret tru;
}
int func1(int a){
    ++a;
    if (a==3){
        print << "base case!\n";
        ret 1;
    }
    func1(a);
}

int main() {
    int a;
    bool b;
    bool c;
    c = tru;
    print << "test arithmetic";
    print << "\n";
    arithmetic();
    retVoid();
    print << "test logic";
    print << "\n";
    b = logic();
    print << b;
    print << "\n";
    print << "test controflow";
    print << "\n";
    if(c){
        print << "should be print\n";
    }
    if(c){
        print << "should be print\n";
    }else{
        print << "should not be print\n";
    }
    if(fls){
        print << "should not be print\n";
    }
    if(fls){
        print << "should not be print\n";
    }else{
        print << "should be print\n";
    }
    print << "test controflow - string";
    print << "\n";
    c = "abc"=="abc"; 
    print << c;
    print << "\n";
    c = c==c;
    print << c;
    print << "\n";
    if ("abc"=="abc"){
        print << "should be print\n";
    }
    a = 3;
    while (a>0){
        print << a;
        print << " ";
        --a;
    }
    print << "test relational";
    print << "\n";
    c = (1<2) && (2<=2) && (2>=2) && (2>1) && (3!=1) && (3==3);
    print << c;
    print << "\n";
    print << "test functions (recursion, call/ret)";
    print << "\n";
    a = func1(0);
    a = test(10, 20);
    print << a;
    print << "\n";
    ret 0;
}
