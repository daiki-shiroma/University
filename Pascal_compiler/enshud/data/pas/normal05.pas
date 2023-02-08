program testBasic;
var i: integer;
    a: array[1..10] of integer;

begin
    i := 1;
    while i <= 10 do
    begin
        a[i] := i * i;
        i := i + 1;
    end;
    writeln(a[5]);
end.
