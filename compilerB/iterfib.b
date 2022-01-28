int func(int n) {
    int a;
    int b;
    a = 0;
    b = 1;
    if (n <= 1) {
        ret n;
    }
    while (n >= 2) {
        int t;
        t = a;
        a = b;
        b = t + b;
        --n;
    }
    ret b;
}

void main() {
    print << func(10);
}

