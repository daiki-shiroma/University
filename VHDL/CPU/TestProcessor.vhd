-------------------------------------------------------------------------------
--
-- VHDL 
-- 
-- Testbench for miniCPU
-- 
-------------------------------------------------------------------------------
--
-- CopyRight (c) KOBAYASHI, Shinsuke
--
--              All Rights Reserved
-- 
-- Update by Keishi SAKANUSHI
-- 2005/10/06
-------------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_textio.all;

use STD.textio.all;

entity TestProcessor is
end TestProcessor;

architecture behavior of TestProcessor is
  signal clock   : std_logic;
  signal reset   : std_logic;
  signal datain  : std_logic_vector(7 downto 0);
  signal dataout : std_logic_vector(7 downto 0);
  signal address : std_logic_vector(15 downto 0);
  signal read    : std_logic;
  signal write   : std_logic;

-------------------------------------------------------------------------------
-- component declaration ------------------------------------------------------
-------------------------------------------------------------------------------
  component TinyProcessor
  port (
    clock   : in  std_logic;
    reset   : in  std_logic;
    DataIn  : in  std_logic_vector (7 downto 0);
    DataOut : out std_logic_vector (7 downto 0);
    address : out std_logic_vector (15 downto 0);
    read    : out std_logic;
    write   : out std_logic
  );
  end component;

-------------------------------------------------------------------------------
-- constant declaration -------------------------------------------------------
-------------------------------------------------------------------------------
  constant clk_cycle : time := 10 ns;   -- frequency 100 MHz

  constant M_MAX : integer := 16#10000#;  -- max size

  file IMin     : std.textio.text is in "memory.txt";         -- VHDL'87
  file Tout     : std.textio.text is out "TestData.out";  -- VHDL'87

  type type_Signals is array(0 to 5) of string(1 to 27);
  constant Str_Signals : type_Signals := (
    ("---------+-+----+--+--+-+-+"),
    ("           < MEMORY >      "),
    ("         Reset       out we"),
    ("  Time NS |addr data  |re| "),
    ("          | 3210 10 10|| | "),
    ("---------+-+----+--+--+-+-+")
    );
-------------------------------------------------------------------------------
-- signal declaration ---------------------------------------------------------
-------------------------------------------------------------------------------

  subtype Dtype is std_logic_vector(7 downto 0);
  type Mtype is array (0 to M_MAX-1) of Dtype;
  
begin

  Processor:TinyProcessor
    port map (  clock   => clock,
                reset   => reset,
                DataIn  => datain,
                DataOut => dataout,
                address => address,
                read    => read,
                write   => write
    );

-------------------------------------------------------------------------------
-- Clock and Reset Signal
-------------------------------------------------------------------------------
  process
  begin
    clock <= '1';
    wait for clk_cycle/2;
    clock <= '0';
    wait for clk_cycle/2;
  end process;

  reset    <= '1',
              '0' after clk_cycle * 2;

-------------------------------------------------------------------------------
-- Instruction and Data Memory
-------------------------------------------------------------------------------
  IDMem : process(clock, reset) 
    variable addr : std_logic_vector(15 downto 0);
    variable data : Dtype;
    variable lbuf : line;
    variable IDM  : Mtype;
    variable data_in_tmp : std_logic_vector(7 downto 0);
  begin  -- IM Access
    if (reset'event and reset = '0') then
      for A in IDM'range loop
        IDM(A) := (others => '0');
      end loop;
      while (not(endfile(IMin))) loop
        readline(IMin, lbuf);
        hread(lbuf, addr);
        hread(lbuf, data);
        IDM(conv_integer(addr(15 downto 0))) := data(7 downto 0);
      end loop;
    end if;

    data_in_tmp := IDM(conv_integer(address(15 downto 0)));

    if (write = '0') then                  -- Write
      IDM(conv_integer(address(15 downto 0))) := dataout;
    end if;
    
    datain <= data_in_tmp;
  end process IDMem;

  
end behavior;


