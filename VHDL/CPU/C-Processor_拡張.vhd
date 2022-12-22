--------------------------------
--  Subset of C Processor     --
--                            --
--       (c) Keishi SAKANUSHI --
--------------------------------
library IEEE;
use IEEE.std_logic_1164.all;

 
entity CProcessor is
  port (
    clock   : in std_logic;
    reset   : in  std_logic;
    DataIn  : in  std_logic_vector (7 downto 0);
    DataOut : out std_logic_vector (7 downto 0);
    Address : out std_logic_vector (15 downto 0);
    read    : out std_logic;
    write   : out std_logic
  );
end CProcessor;

architecture logic of CProcessor is
  component DataPath
    port (
    DataIn    : in  std_logic_vector (7 downto 0);
    selMuxDIn : in  std_logic;
    selMuxDOut: in  std_logic_vector (1 downto 0);

    loadhIX   : in  std_logic;
    loadlIX   : in  std_logic;
    loadhMB   : in  std_logic;
    loadlMB   : in  std_logic;

    loadIR    : in  std_logic;
    IRout     : out std_logic_vector (7 downto 0);

    loadIP    : in  std_logic;
    incIP     : in  std_logic;
    inc2IP    : in  std_logic;
    clearIP   : in  std_logic;

    selMuxAddr: in  std_logic;
    Address   : out std_logic_vector (15 downto 0);
    CarryF    : out std_logic;
    ZeroF     : out std_logic;
    DataOut   : out std_logic_vector (7 downto 0);

    loadRegA  : in  std_logic;
    loadRegB  : in  std_logic;
    loadRegC  : in  std_logic;

    modeALU   : in  std_logic_vector (3 downto 0);
    loadFZ    : in  std_logic;
    loadFC    : in  std_logic;

    clock     : in  std_logic;
    reset     : in  std_logic;

    selMuxDOutAdd : in  std_logic
    );
  end component;
  
  component Controler
    port (
    selMuxDIn : out std_logic;

    selMuxDOut: out std_logic_vector (1 downto 0); --------

    loadhMB   : out std_logic;
    loadlMB   : out std_logic;
    loadhIX   : out std_logic;
    loadlIX   : out std_logic;

    loadIR    : out std_logic;
    IRout     : in  std_logic_vector (7 downto 0);

    loadIP    : out std_logic;
    incIP     : out std_logic;
    inc2IP    : out std_logic;
    clearIP   : out std_logic;

    selMuxAddr: out std_logic;
    ZeroF     : in  std_logic;
    CarryF    : in  std_logic;

    loadRegC  : out std_logic;
    loadRegB  : out std_logic;
    loadRegA  : out std_logic;

    modeALU   : out std_logic_vector (3 downto 0);
    loadFZ    : out std_logic;
    loadFC    : out std_logic;

    read      : out std_logic;
    write     : out std_logic;

    clock     : in  std_logic;
    reset     : in  std_logic;

    selMuxDOutAdd : out  std_logic 
    );
  end component;

signal    selMuxDIn : std_logic;
signal    selMuxDOut : std_logic_vector (1 downto 0);

signal    loadhIX   : std_logic;
signal    loadlIX   : std_logic;
signal    loadhMB   : std_logic;
signal    loadlMB   : std_logic;

signal    loadIR    : std_logic;
signal    IRout     : std_logic_vector (7 downto 0);

signal    loadIP    : std_logic;
signal    incIP     : std_logic;
signal    inc2IP    : std_logic;
signal    clearIP   : std_logic;

signal    selMuxAddr: std_logic;
signal    CarryF    : std_logic;
signal    ZeroF     : std_logic;

signal    loadRegA  : std_logic;
signal    loadRegB  : std_logic;
signal    loadRegC  : std_logic;

signal    modeALU   : std_logic_vector (3 downto 0);
signal    loadFC  : std_logic;
signal    loadFZ  : std_logic;

signal    selMuxDOutAdd : std_logic;

begin    -- logic


path : DataPath
  port map(
    DataIn    => DataIn,
    selMuxDIn  => selMuxDIn,
    selMuxDOut     => selMuxDOut,

    loadlIX     => loadlIX,
    loadhIX   => loadhIX,
    loadhMB    => loadhMB,
    loadlMB   => loadlMB,

    loadIR   => loadIR,
    IRout => IRout,

    loadIP  => loadIP,
    incIP   => incIP,
    inc2IP    => inc2IP,
    clearIP    => clearIP,

    selMuxAddr=> selMuxAddr,
    Address   => Address,
    DataOut   => DataOut,
    CarryF   => CarryF,
    ZeroF   => ZeroF, 
    
    loadRegA    => loadRegA,
    loadRegB     => loadRegB,
    loadRegC    => loadRegC,
    loadFC   => loadFC,
    loadFZ   => loadFZ,
    modeALU    => modeALU,

    clock     => clock,
    reset     => reset,


    selMuxDOutAdd => selMuxDOutAdd   -----

  );

ctrl : Controler
  port map(

selMuxDIn => selMuxDIn,

 selMuxDOut=> selMuxDOut, --------

        loadhMB   => loadhMB,
loadlMB   => loadlMB,
loadhIX   => loadhIX,
loadlIX   => loadlIX,

    loadIR    => loadIR,
IRout     => IRout,

    loadIP    => loadIP,
    incIP     => incIP,
    inc2IP    => inc2IP,
    clearIP   => clearIP,

    selMuxAddr=> selMuxAddr,
    ZeroF     => ZeroF,
CarryF    => CarryF,

    loadRegA  => loadRegA,
    loadRegB  => loadRegB,
    loadRegC  => loadRegC,

    modeALU   => modeALU,
loadFZ    => loadFZ,
        loadFC    => loadFC,

    clock     => clock,
reset     => reset,

    read      => read,
    write     => write,


    selMuxDOutAdd => selMuxDOutAdd   -----


  );

end logic;