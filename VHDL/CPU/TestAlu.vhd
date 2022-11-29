--------------------------------
--  DataPath of C Processor   --
--                            --
--       (c) Keishi SAKANUSHI --
--------------------------------
library IEEE;
use IEEE.std_logic_1164.all;

entity TestAlu is
end TestAlu;

architecture behavior of TestAlu is
  constant STEP : Time := 100 ns;

  signal In_A     : std_logic_vector(7 downto 0);
  signal In_B     : std_logic_vector(7 downto 0);
  signal Cin      : std_logic;
  signal modeALU  : std_logic_vector(1 downto 0);
  signal Out_F    : std_logic_vector(7 downto 0);
  signal Out_C    : std_logic;
  signal Out_Z    : std_logic;

  component ALU08
    port (
      a       : in  std_logic_vector(7 downto 0);
      b       : in  std_logic_vector(7 downto 0);
      cin     : in  std_logic;
      mode    : in  std_logic_vector(1 downto 0);
      fout    : out std_logic_vector(7 downto 0);
      cout    : out std_logic;
      zout    : out std_logic
    );
  end component;

begin
  ALU: ALU08
  port map(
    a     => In_A,
    b     => In_B,
    cin   => Cin,
    mode  => modeALU,
    fout  => Out_F,
    cout  => Out_C,
    zout  => Out_Z
  );
  
  
  process
    begin
      In_A <= "11110000";
      In_B <= "00001111";
      Cin  <= '0';
      modeALU <= "00";
      wait for STEP;
      modeALU <= "01";
      wait for STEP;
      modeALU <= "10";
      wait for STEP;
      modeALU <= "11";
      wait for STEP;
      In_A    <= "00000010";
      In_B    <= "00000011";
      modeALU <= "00";
      wait for STEP;
      modeALU <= "01";
      wait for STEP;
      modeALU <= "10";
      wait for STEP;
      modeALU <= "11";
      wait for STEP;
    wait;
  end process;
end behavior;

