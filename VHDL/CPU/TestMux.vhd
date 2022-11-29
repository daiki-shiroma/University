--------------------------------
--  DataPath of C Processor   --
--                            --
--       (c) Keishi SAKANUSHI --
--------------------------------
library IEEE;
use IEEE.std_logic_1164.all;

entity TestMux is
end TestMux;

architecture behavior of TestMux is
  constant STEP : Time := 100 ns;

  signal In_A     : std_logic_vector(7 downto 0);
  signal In_B     : std_logic_vector(7 downto 0);
  signal Sel      : std_logic;
  signal Q        : std_logic_vector(7 downto 0);

  component Mux2x08
    port (
      a   : in  std_logic_vector(7 downto 0); 
      b   : in  std_logic_vector(7 downto 0);
      sel : in  std_logic;
      q   : out std_logic_vector(7 downto 0)
    ); 
  end component;

begin
  Selector: Mux2x08 
  port map(
    a => IN_A,
    b => IN_B,
    sel => Sel,
    q => Q
  );
  
  In_A <= "11110000";
  In_B <= "00001111";
  
  process
    begin
      Sel <= '0';
      wait for STEP;
      Sel <= '1';
      wait for STEP;
      Sel <= '0';
      wait for STEP;
      Sel <= '1';
      wait for STEP;
    wait;
  end process;
end behavior;

