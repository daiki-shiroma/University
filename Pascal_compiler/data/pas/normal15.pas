program weekday;
var table: array[1..12] of integer;
    Year, Month, Day, Result: integer;

procedure dayofWeek(Y, M, D: integer);
var z, N: integer;
begin
    if M = 1 then
    begin
        z := Y-1901;
    end
    else
    begin
        if M = 2 then
        begin
            z := Y-1901;
        end
        else
        begin
            z := Y-1900;
        end;
    end;

    N := (4+z+(z div 4)+table[M]+(D-1));
    while N >= 7 do
    begin
        N := N-7;
    end;
    Result := N;
end;

procedure printDay(Y, M, D, DD: integer);
begin
    writeln(Y, '-', M, '-', D, ' IS');
    if DD = 0 then
    begin
        writeln('SUNDAY');
    end
    else
    begin
        if DD = 1 then
        begin
            writeln('MONDAY');
        end
        else
        begin
            if DD = 2 then
            begin
                writeln('TUESDAY');
            end
            else
            begin
                if DD = 3 then
                begin
                    writeln('WEDNESDAY');
                end
                else
                begin
                    if DD = 4 then
                    begin
                        writeln('THURSDAY');
                    end
                    else
                    begin
                        if DD = 5 then
                        begin
                            writeln('FRIDAY');
                        end
                        else
                        begin
                            if DD = 6 then
                            begin
                                writeln('SATURDAY');
                            end;
                        end;
                    end;
                end;
            end;
        end;
    end;
end;

begin
    table[1]  := 5;
    table[2]  := 1;
    table[3]  := 0;
    table[4]  := 3;
    table[5]  := 5;
    table[6]  := 1;
    table[7]  := 3;
    table[8]  := 6;
    table[9]  := 2;
    table[10] := 4;
    table[11] := 0;
    table[12] := 2;

    Year := 1991;
    Month := 1;
    Day := 1;
    dayofWeek(Year, Month, Day);
    printDay(Year, Month, Day, Result);

    Year := 2017;
    Month := 9;
    Day := 28;
    dayofWeek(Year, Month, Day);
    printDay(Year, Month, Day, Result);
end.
