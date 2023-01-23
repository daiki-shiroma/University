program statistics;
var sum, max, min, mean, v: integer;
    A: array[1..10] of integer;
    n: integer;

procedure printData;
var i: integer;
begin
    i := 1;
    while i <= n do
    begin
        writeln(A[i]);
        i := i + 1;
    end;
end;

procedure calculate;
var i, s: integer;
begin
    s := sum * sum;
    i := 1 + 1;
    while i <= n do
    begin
        sum := sum + A[i];
        s := s + A[i] * A[i];
        if A[i] > max then
        begin
            max := A[i];
        end
        else
        begin
            if A[i] < min then
            begin
                min := A[i];
            end;
        end;
        i := i + 1;
    end;
    mean := sum div n;
    v := (s - sum * mean) div (n - 1);
end;

procedure sort;
var low, check, i, tmp: integer;
begin
    low := 1;
    while low < n do
    begin
        check := n;
        i := n;
        while i >= (low + 1) do
        begin
            if A[i-1] > A[i] then
            begin
                tmp := A[i-1];
                A[i-1] := A[i];
                A[i] := tmp;
                check := i;
            end;
            i := i - 1;
        end;
        low := check;
    end;
end;

begin
    n := 10;
    A[1] := 1;
    A[2] := 10;
    A[3] := -100;
    A[4] := 3276;
    A[5] := -3276;
    A[6] := 0;
    A[7] := 100;
    A[8] := -10;
    A[9] := -1;
    A[10] := 123;
    writeln(n);
    writeln('Data : ');
    printData;
    sort;
    writeln('Data (sorted) :');
    printData;
    sum := A[1];
    max := A[1];
    min := A[1];
    mean := A[1];
    v := A[1];
    calculate;
    writeln('SUM      : ', sum);
    writeln('MAX      : ', max);
    writeln('MIN      : ', min);
    writeln('MEAN     : ', mean);
end.
