program sin;
var x: integer;
    y: array[1..41] of integer;
    out: array[1..11] of integer;
    scale: integer;
    i: integer;
    point: integer;
    po1: integer;
    po2: integer;

begin
    scale := 1;
    i := 1;
    while i <= 11 do
    begin
        out[i] := 11111;
        i := i+1;
    end;

    x := 1;
    while x <= 41 do
    begin
        y[x] := (x-21)*(x-21)-180;
        if y[x] > scale then
        begin
            scale := y[x];
        end
        else
        begin
            if -y[x] > scale then
            begin
                scale := -y[x];
            end;
        end;
        x := x+1;
    end;

    x := 1;
    while x <= 41 do
    begin
        out[5] := 11110;
        point := y[x]*25 div scale+25;
        po1 := point div 5;
        po2 := point-po1*5;
        po1 := po1+1;
        if po1 = 5 then
        begin
            if po2 = 0 then
            begin
                out[po1] := 31110;
            end
            else
            begin
                if po2 = 1 then
                begin
                    out[po1] := 13110;
                end
                else
                begin
                    if po2 = 2 then
                    begin
                        out[po1] := 11310;
                    end
                    else
                    begin
                        if po2 = 3 then
                        begin
                            out[po1] := 11130;
                        end
                        else
                        begin
                            if po2 = 4 then
                            begin
                                out[po1] := 11113;
                            end;
                        end;
                    end;
                end;
            end;
        end
        else
        begin
            if po2 = 0 then
            begin
                out[po1] := 31111;
            end
            else
            begin
                if po2 = 1 then
                begin
                    out[po1] := 13111;
                end
                else
                begin
                    if po2 = 2 then
                    begin
                        out[po1] := 11311;
                    end
                    else
                    begin
                        if po2 = 3 then
                        begin
                            out[po1] := 11131;
                        end
                        else
                        begin
                            if po2 = 4 then
                            begin
                                out[po1] := 11113;
                            end;
                        end;
                    end;
                end;
            end;
        end;
        writeln(out[1], out[2], out[3], out[4], out[5], out[6],
                out[7], out[8], out[9], out[10], out[11], '  ', x);
        x := x+1;
        out[po1] := 11111;
    end;
end.
