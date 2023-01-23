program testAdv;
var i: integer;
    a: array[1..10] of integer;
    msg: char;
    debug: boolean;

procedure printData;
var  i: integer;
begin
    i := 1;
    while i <= 10 do
    begin
        writeln('[', msg, '] i=', a[i]);
        i := i + 1;
    end;
end;

{ comment test here        }
{ begin for if end writeln }
{ i=1; i='a'; ..., ., : ;* }

begin
    debug := true;
    msg := 'D';
    writeln('***** calculating ******');
    i := 1;
    while i <= 10 do
    begin
        a[i] := i * i;
        if msg          then begin writeln(i); end;  { <<<< sematic error here }
        i := i + 1;
    end;
    writeln('***** result ******');
    printData;
end.
