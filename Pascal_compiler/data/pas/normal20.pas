program magicSquare;
var x: integer;
    y: integer;
    a: integer;
    i: integer;
    n: integer;
    max: integer;
    s: array [1..25] of integer;
    sum: integer;

begin
    a := 5;
    max := 25;
    n := 1;
    x := (a + 1) div 2;
    y := x + 1 - a;
    while n <= max do
    begin
        i := 1;
        while i <= a do
        begin
            if x < 1 then
            begin
                x := x + a;
            end
            else
            begin
                if x > a then
                begin
                    x := x - a;
                end;
            end;
            if y < 1 then
            begin
                y := y + a;
            end
            else
            begin
                if y > a then
                begin
                    y := y - a;
                end;
            end;
            s[x + (y - 1) * a] := n;
            x := x - 1;
            y := y + 1;
            i := i + 1;
            n := n + 1;
        end;
        x := x + 1;
        y := y + 1;
    end;
    i := 0;
    writeln('               **** magic square ****');
    while i < a do
    begin
        writeln('      +-------+-------+-------+-------+-------+');
        writeln('      |       |       |       |       |       |');
        writeln('      |  ', s[1+i*a], '   |  ', s[2+i*a], '   |  ', s[3+i*a],
                '   |  ', s[4+i*a], '   |  ', s[5+i*a], '   |');
        writeln('      |       |       |       |       |       |');
        i := i + 1;
    end;
    writeln('      +-------+-------+-------+-------+-------+');
    sum := (1 + max) * max div (2 * a);
    writeln('sum of integers of lines, columns, or diagonals  = ', sum);
end.
