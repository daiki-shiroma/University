program primeFactorization;
var FACTOR: array [1..30] of integer;
    TIMES: array [1..30] of integer;
    DATA: array [1..10] of integer;
    NUM: integer;
    i, j, k: integer;
    m, n: integer;
begin
    m := 17;
    n := 23;
    DATA[1] := 345;
    i := 2;
    while i <= 10 do
    begin
        DATA[i] := DATA[i-1] * m + n;
        while DATA[i] > 1000 do
        begin
            DATA[i] := DATA[i] - 1000;
        end;
        i := i + 1;
    end;
    k := 1;
    while k <= 10 do
    begin
        j := 2;
        i := 0;
        NUM := DATA[k];
        while NUM <> 1 do
        begin
            if NUM div j*j = NUM then
            begin
                i := i + 1;
                FACTOR[i] := j;
                TIMES[i] := 1;
                NUM := NUM div j;
            end;
            while NUM div j*j = NUM do
            begin
                TIMES[i] := TIMES[i] + 1;
                NUM := NUM div j;
            end;
            if j = 2 then
            begin
                j := 3;
            end
            else
            begin
                j := j + 2;
            end;
        end;
        j := 1;
        writeln(' ANARISE ', DATA[k], ' = ');
        while j <= i do
        begin
            writeln('               ', FACTOR[j], '^', TIMES[j]);
            j := j + 1;
        end;
        k := k + 1;
    end;
end.
