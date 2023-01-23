program quicksort;
var v: array[1..100] of integer;
    lv: array[1..20] of integer;
    uv: array[1..20] of integer;
    p: integer;
    pivlin: integer;
    temp: integer;
    i: integer;
    j: integer;
    n: integer;

begin
    n := 20;
    writeln('***** quick sort *****');
    v[1] := 12;
    i := 2;
    while i <= n do
    begin
        v[i] := v[i-1] * 65 + 17;
        v[i] := v[i] - v[i] div 256 * 256;
        i := i + 1;
    end;
    writeln('***** data ******');
    i := 1;
    while i <= n do
    begin
        writeln(v[i], ' ', v[i+1], ' ', v[i+2], ' ', v[i+3], ' ', v[i+4]);
        i := i + 5;
    end;
    lv[1] := 1;
    uv[1] := n;
    p := 1;
    while p > 0 do
    begin
        if lv[p] >= uv[p] then
        begin
            p := p - 1;
        end
        else
        begin
            i := lv[p] - 1;
            j := uv[p];
            pivlin := v[j];
            while i < j do
            begin
                i := i + 1;
                while v[i] < pivlin do
                begin
                    i := i + 1;
                end;
                j := j - 1;
                while v[j] > pivlin do
                begin
                    j := j - 1;
                    if i >= j then
                    begin
                        pivlin := v[j];
                    end;
                end;
                if i < j then
                begin
                    temp := v[i];
                    v[i] := v[j];
                    v[j] := temp;
                end;
            end;
            temp := v[i];
            v[i] := v[uv[p]];
            v[uv[p]] := temp;
            if i-lv[p] < uv[p] - i then
            begin
                lv[p+1] := lv[p];
                uv[p+1] := i - 1;
                lv[p] := i + 1;
            end
            else
            begin
                lv[p+1] := i + 1;
                uv[p+1] := uv[p];
                uv[p] := i - 1;
            end;
            p := p + 1;
        end;
    end;
    writeln('***** result ******');
    i := 1;
    while i <= n do
    begin
        writeln(v[i], ' ', v[i+1], ' ', v[i+2], ' ', v[i+3], ' ', v[i+4]);
        i := i + 5;
    end;
end.
