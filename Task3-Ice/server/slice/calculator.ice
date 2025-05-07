
#ifndef CALC_ICE
#define CALC_ICE

module Demo
{
  enum operation { MIN, MAX, AVG };
  
  exception NoInput {};

  struct A
  {
    short a;
    long b;
    float c;
    string d;
  };

  sequence<int> numbersSequence;

  exception EmptySequence
  {
    string reason = "Sequence is empty";
  };

  interface Calc
  {
    idempotent long add(int a, int b);
    idempotent long subtract(int a, int b);
    idempotent double avg(numbersSequence numbers) throws EmptySequence;
    void op(A a1, short b1); //załóżmy, że to też jest operacja arytmetyczna ;)
  };

};

#endif
