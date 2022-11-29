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
    IRout     : out std_logic_vector (7 downto 0);
    Address   : out std_logic_vector (15 downto 0);
    DataOut   : out std_logic_vector (7 downto 0);

    CarryF    : out std_logic;
    ZeroF     : out std_logic;

    modeALU   : in  std_logic_vector (3 downto 0);
    selMuxDOut : in   std_logic_vector (1 downto 0);
    loadRegA  : in  std_logic;
    loadRegB  : in  std_logic;
    loadRegC  : in  std_logic;

    selMuxAddr: in  std_logic;
    loadIR    : in  std_logic;
    loadhIX   : in  std_logic;
    loadlIX   : in  std_logic;
    loadhMB   : in  std_logic;
    loadlMB   : in  std_logic;
 
    selMuxDIn : in  std_logic;
    loadIP    : in  std_logic;
    incIP     : in  std_logic;
    inc2IP    : in  std_logic;
    clearIP   : in  std_logic;

    loadFZ    : in  std_logic;
    loadFC   : in  std_logic;

    clock     : in  std_logic;
    reset     : in  std_logic
    );
  end component;
  
  component Controler
    port (
    selMuxDIn : out std_logic;

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
    reset     : in  std_logic	
    );
  end component;


signal    IRout     : std_logic_vector (7 downto 0);

signal    CarryF    : std_logic;
signal    ZeroF     : std_logic;

signal    modeALU   : std_logic_vector (3 downto 0);
signal    selMuxDOut : std_logic_vector (1 downto 0);
signal    loadRegA  : std_logic;
signal    loadRegB  : std_logic;
signal    loadRegC  : std_logic;
signal    loadFC  : std_logic;
signal    loadFZ  : std_logic;

signal    selMuxAddr: std_logic;
signal    loadIR    : std_logic;
signal    loadhIX   : std_logic;
signal    loadlIX   : std_logic;
signal    loadhMB   : std_logic;
signal    loadlMB   : std_logic;

signal    selMuxDIn : std_logic;
signal    loadIP    : std_logic;
signal    incIP     : std_logic;
signal    inc2IP    : std_logic;
signal    clearIP   : std_logic;


begin    -- logic


path : DataPath
  port map(
    DataIn    => DataIn,
    IRout => IRout,
	
    Address   => Address,
    DataOut   => DataOut,
    CarryF   => CarryF,
    ZeroF   => ZeroF,
	
    modeALU    => modeALU,
    selMuxDOut     => selMuxDOut,
	
    loadRegA    => loadRegA,
    loadRegB     => loadRegB,
    loadRegC    => loadRegC,
    loadFC   => loadFC,
    loadFZ   => loadFZ,

    selMuxAddr=> selMuxAddr,
    loadIR   => loadIR,
    loadlIX     => loadlIX,
    loadhMB    => loadhMB,
    loadlMB   => loadlMB,
	
    selMuxDIn  => selMuxDIn,
    loadIP  => loadIP,
    incIP   => incIP,
    inc2IP    => inc2IP,
    clearIP    => clearIP,
	
    clock     => clock,
    reset     => reset
  );

ctrl : Controler
  port map(

	selMuxDIn => selMuxDIn,
	
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
    write     => write
  );

end logic;