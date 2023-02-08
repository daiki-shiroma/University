program testBasic;
var f: boolean;

begin
    f := true;
    if not f = true then
    begin
        writeln('then');
    end
    else
    begin
        writeln('else');
    end;
end.
