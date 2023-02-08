program testBasic;
var i: integer;
    c: char;

procedure subproc;
var  c: char;
begin
    c := 's';
    writeln(c);
end;

begin
    c := 'm';
    subproc;
    writeln(c);
end.
