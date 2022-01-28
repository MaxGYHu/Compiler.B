int ret1(int a, int b, int c, int d) {
    ret a;
}

int ret2(int a, int b, int c, int d) {
    ret b;
}

int ret3(int a, int b, int c, int d) {
    ret c;
}

int ret4(int a, int b, int c, int d) {
    ret d;
}

void main() {
    int a;
    int b;
    int c;
    int d;
    a = 10;
    b = 20;
    c = 30;
    d = 40;
    print << ret1(a, b, c, d);
    print << ret2(a, b, c, d);
    print << ret3(a, b, c, d);
    print << ret4(a, b, c, d);
}
