--------------------------------
-- DataPath of Tiny Processor --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2005/08/23 --
--------------------------------
library IEEE;
use IEEE.std_logic_1164.all;

entity DataPath is
  port (

    DataIn    : in  std_logic_vector (7 downto 0);
    selMuxDIn : in  std_logic;

    loadhMB   : in  std_logic;
    loadlMB   : in  std_logic;
    loadhIX   : in  std_logic;
    loadlIX   : in  std_logic;

    loadIR    : in  std_logic;
    IRout     : out std_logic_vector (7 downto 0);

    loadIP    : in  std_logic;
    incIP     : in  std_logic;
    inc2IP    : in  std_logic;
    clearIP   : in  std_logic;

    selMuxAddr: in  std_logic;
    Address   : out std_logic_vector (15 downto 0);
    ZeroF     : out std_logic;
    CarryF    : out std_logic;
    DataOut   : out std_logic_vector (7 downto 0);

    loadRegC  : in  std_logic;
    loadRegB  : in  std_logic;
    loadRegA  : in  std_logic;

    modeALU   : in  std_logic_vector (3 downto 0);
    loadFZ    : in  std_logic;
    loadFC    : in  std_logic;

    clock     : in  std_logic;
    reset     : in  std_logic;


    selMuxDOut: in   std_logic_vector (1 downto 0);

    selMuxDOutAdd: in std_logic   ----------add

 
   );
  end DataPath;
  
 


architecture logic of DataPath is
component Register01  --FC, FZ
  port (
    d     : in  std_logic;
    load  : in  std_logic;
    clock : in std_logic;
    reset : in  std_logic;
    q     : out std_logic
  );
end component;

component Register08  --A,B,C, IR
  port (
    d     : in  std_logic_vector(7 downto 0);
    load  : in  std_logic;
    clock : in  std_logic;
    reset : in  std_logic;
    q     : out std_logic_vector(7 downto 0)
  );
end component; 

component Register16in2 -- IX, MB
  port (
    dH    : in  std_logic_vector(7 downto 0);
    dL    : in  std_logic_vector(7 downto 0);
    loadh : in  std_logic;
    loadl : in  std_logic;
    clock : in  std_logic;
    reset : in  std_logic;
    q     : out std_logic_vector(15 downto 0)
  );
end component;
 
component ALU08  --ALU
  port (
    a       : in  std_logic_vector(7 downto 0);
    b       : in  std_logic_vector(7 downto 0);
    cin     : in  std_logic;
    mode    : in  std_logic_vector(3 downto 0);
    fout    : out std_logic_vector(7 downto 0);
    cout    : out std_logic;
    zout    : out std_logic
  );
end component;

component Counter16  --IP
  port (
    clock : in  std_logic;
    load  : in  std_logic;
    d     : in  std_logic_vector(15 downto 0);
    inc   : in  std_logic;
    inc2  : in  std_logic;
    clear : in  std_logic;
    reset : in  std_logic;
    q     : out std_logic_vector(15 downto 0)
  );
end component;

component mux2x08  --MuxDIn, RegBorRegC--new
   port (
    a   : in  std_logic_vector(7 downto 0);
    b   : in  std_logic_vector(7 downto 0);
    sel : in  std_logic;
    q   : out std_logic_vector(7 downto 0)
  );
end component;



component mux2x16  --MuxAddr, 
  port (
    a   : in  std_logic_vector(15 downto 0); 
    b   : in  std_logic_vector(15 downto 0);
    sel : in  std_logic;
    q   : out std_logic_vector(15 downto 0)
   );
end component;

component mux4x08  --MuxDOut, 
  port (
    a   : in  std_logic_vector(7 downto 0); 
    b   : in  std_logic_vector(7 downto 0);
    c   : in  std_logic_vector(7 downto 0); 
    d   : in  std_logic_vector(7 downto 0);
    sel : in  std_logic_vector(1 downto 0);
    q   : out std_logic_vector(7 downto 0)
   );
end component;



signal qRegA      : std_logic_vector(7 downto 0);
signal qRegB      : std_logic_vector(7 downto 0);
signal qRegC      : std_logic_vector(7 downto 0);
signal qMB        : std_logic_vector(15 downto 0);
signal qIX        : std_logic_vector(15 downto 0);
signal qIP        : std_logic_vector(15 downto 0);
signal foutALU    : std_logic_vector(7 downto 0);
signal coutALU    : std_logic;
signal zoutALU    : std_logic;
signal DataInTmp  : std_logic_vector(7 downto 0);

signal DataInTmpAdd  : std_logic_vector(7 downto 0); --- add

--------------------------------

signal zero     : std_logic;


begin
zero <= '0';

--------------------------------
-- Selector to Data Bus In    --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
MuxDIn : mux2x08
  port map (
    a   => foutALU,
    b   => DataIn,
    sel => selMuxDIn,
    q   => DataInTmp
  );







--------------------------------
-- Selector to Data Bus In    --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
RegBorRegC : mux2x08
  port map (
    a   => qRegB,
    b   => qRegC,
    sel => selMuxDoutAdd,
    q   => DataInTmpAdd
  );









--------------------------------
--                            --
--  Register A                --
--                            --
--------------------------------
RegA : Register08
  port map(
    d     => DataInTmp,
    load  => loadRegA,
    clock => clock,
    reset => reset,
    q     => qRegA
  );


--------------------------------
--                            --
--  Register B                --
--                            --
--------------------------------
RegB : Register08
  port map(
    d     => DataInTmp,
    load  => loadRegB,
    clock => clock,
    reset => reset,
    q     => qRegB
  );

--------------------------------
--                            --
--  Register C                --
--                            --
--------------------------------
RegC : Register08
  port map(
    d     => DataInTmp,
    load  => loadRegC,
    clock => clock,
    reset => reset,
    q     => qRegC
  );


--------------------------------
--                            --
--  MB Register               --
--                            --
--------------------------------
MB : Register16in2
  port map(
    dh    => DataIn,
    dl    => DataIn,
    loadh => loadhMB,
    loadl => loadlMB,
    clock => clock,
    reset => reset,
    q     => qMB
  );


--------------------------------
-- Index pointer              --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
IX : Register16in2 
  port map(
    dh    => DataIn,
    dl    => DataIn,
    loadh => loadhIX,
    loadl => loadlIX,
    clock => clock,
    reset => reset,
    q     => qIX
  );

--------------------------------
--                            --
--  ALU                       --
--                            --
--------------------------------
ALU : ALU08
  port map(
    a       => qRegA,
    b       => DataInTmpAdd, ---changed
    cin     => zero,
    mode    => modeALU,
    fout    => foutALU,
    cout    => coutALU,  
    zout    => zoutALU
  );


--------------------------------
-- Instruction Pointer        --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
IP : Counter16
  port map(
    d     => qMB,
    inc   => incIP,
    inc2  => inc2IP,
    clear => clearIP,
    load  => loadIP,
    clock => clock,
    reset => reset,
    q     => qIP
  );


--------------------------------
--                            --
--  Instruction Register      --
--                            --
--------------------------------
IR : Register08
  port map(
    d     => DataIn,
    load  => loadIR,
    clock => clock,
    reset => reset,
    q     => IRout
  );


--------------------------------
-- Zero Flag                  --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
FZ : Register01
  port map(
    d     => zoutALU,
    load  => loadFZ,
    clock => clock,
    reset => reset,
    q     => ZeroF
  );

--------------------------------
-- Clear Flag                 --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
FC : Register01
  port map(
    d     => coutALU,   
    load  => loadFC,
    clock => clock,
    reset => reset,
    q     => CarryF
  );


--------------------------------
-- Selector to Address bus    --
--                            --
--       (c) Keishi SAKANUSHI --
--                 2004/08/23 --
--------------------------------
MuxAddr : Mux2x16 
  port map (
    a   => qIP,
    b   => qIX,
    sel => selMuxAddr,
    q   => Address
  );



--------------------------------
-- ADD    --
--                            --
--       (c) SHIROMA     DAIKI--
--                 2022/10/17 --
--------------------------------
MuxDOut : Mux4x08 
  port map (
    a   => qRegA,
    b   => qRegB,
    c   => qRegC,
    d   => "00000000",
    sel => selMuxDOut,
    q   => DataOut
  );


end logic;
