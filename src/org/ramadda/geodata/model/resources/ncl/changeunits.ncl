;*************************************************
; Function: changeUnits
;
; Purpose:  change the units of the data
;
; Params:   dataIn - the data
;           unitsIn - the original units
;           unitsOut - the new units
;
; Return:   the data in the new units
;*************************************************
undef("changeUnits")
function changeUnits(dataIn:float, unitsIn:string, unitsOut:string)
local dataOut

begin

  dataOut = dataIn
  if ((unitsIn .ne. unitsOut) .and. (.not. ismissing(unitsOut) .and. unitsOut .ne. "")) then

    ; Temperature
    if (unitsIn .eq. "K" .or. \
        unitsIn .eq. "degK" .or. \
        unitsIn .eq. "Kelvin" .or. \
        unitsIn .eq. "Kelvins") then
      if (unitsOut .eq. "C" .or. \
          unitsOut .eq. "Cel" .or. \
          unitsOut .eq. "degC" .or. \
          unitsOut .eq. "Celsius") then
        dataOut = dataIn-273.15
        dataOut@units = "degC"
      end if
      if (unitsOut .eq. "F" .or. \
          unitsOut .eq. "degF" .or. \
          unitsOut .eq. "Fahrenheit") then
        dataOut = dataIn-273.15
        dataOut = (9*dataOut/5)+32
        dataOut@units = "F"
      end if
    end if

    if (unitsIn .eq. "C" .or. \
        unitsIn .eq. "Cel" .or. \
        unitsIn .eq. "degC" .or. \
        unitsIn .eq. "Celsius") then
      if (unitsOut .eq. "K" .or. unitsOut .eq. "degK") then
        dataOut = dataIn+273.15
        dataOut@units = "K"
      end if
      if (unitsOut .eq. "F" .or. \
          unitsOut .eq. "degF" .or. \
          unitsOut .eq. "Fahrenheit") then
        dataOut = dataIn-32
        dataOut = (5*dataOut)/9
        dataOut@units = "F"
      end if
    end if

    if (unitsIn .eq. "F" .or. \
        unitsIn .eq. "degF" .or. \
        unitsIn .eq. "Fahrenheit") then
      if (unitsOut .eq. "C" .or. \
          unitsOut .eq. "Cel" .or. \
          unitsOut .eq. "degC" .or. \
          unitsOut .eq. "Celsius") then
          dataOut = (dataIn-32)*5/9
          dataOut@units = "degC"
      end if
      if (unitsOut .eq. "K" .or. unitsOut .eq. "degK") then
        dataOut = (dataIn-32)*5/9 ; convert to degC
        dataOut = dataOut+273.15
        dataOut@units = "K"
      end if
    end if

    ; precipitation  length/time
    if (unitsIn .eq. "kg m-2 s-1" .or. \
        unitsIn .eq. "kg m**-2 s**-1" .or. \
        unitsIn .eq. "kg/m^2/s" .or. \
        unitsIn .eq. "Kg/m^2/s" .or. \
        unitsIn .eq. "mm/s") then
      if (unitsOut .eq. "mm/s") then
        ; just a name change
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "mm/day") then
        dataOut = dataIn*86400
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "m s-1" .or. \
        unitsIn .eq. "m s**-1" .or. \
        unitsIn .eq. "m/s") then
      if (unitsOut .eq. "mm/s") then
        dataOut = dataIn*1000
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "mm/day") then
        dataOut = dataIn*86400000 ; 86400*1000
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "m/day") then
      if (unitsOut .eq. "mm/s") then
        dataOut = dataIn/86.4
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "mm/day") then
        dataOut = dataIn*1000 
        dataOut@units = unitsOut
      end if
    end if

    ; speed
    if (unitsIn .eq. "m s-1" .or. \
        unitsIn .eq. "m s**-1" .or. \
        unitsIn .eq. "m/s") then
      if (unitsOut .eq. "mi/h" .or. \
          unitsOut .eq. "mi h-1" .or. \
          unitsOut .eq. "mph") then
        dataOut = dataIn*2.23694
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "kt" .or. \
          unitsOut .eq. "kts" .or. \
          unitsOut .eq. "knots") then
        dataOut = dataIn*1.94384
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "mi/h" .or. \
        unitsIn .eq. "mi h-1") then
      if (unitsOut .eq. "m/s" .or. \
          unitsOut .eq. "m s-1") then
        dataOut = dataIn*0.44704
        dataOut@units = "m s-1"
      end if
      if (unitsOut .eq. "kt" .or. \
          unitsOut .eq. "kts" .or. \
          unitsOut .eq. "knots") then
        dataOut = dataIn*0.868976
        dataOut@units = unitsOut
      end if
    end if

    ; length
    ;; mm -> cm/in/ft  (short lengths)
    if (unitsIn .eq. "kg m-2" .or. \
        unitsIn .eq. "kg m**-2" .or. \
        unitsIn .eq. "kg/m^2" .or. \
        unitsIn .eq. "Kg/m^2" .or. \
        unitsIn .eq. "mm") then
      if (unitsOut .eq. "mm") then
        ; just a name change
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "cm") then
        dataOut = dataIn/10
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "m") then
        dataOut = dataIn/1000
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "in") then
        dataOut = dataIn/25.4
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "ft") then
        dataOut = dataIn/304.8
        dataOut@units = unitsOut
      end if
    end if

    ;; meters/geopotential meters -> ft/submeters (long_lengths)
    if (unitsIn .eq. "m" .or. \
        unitsIn .eq. "gpm") then
      if (unitsOut .eq. "mm") then
        dataOut = dataIn*1000
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "cm") then
        dataOut = dataIn*100
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "ft") then
        dataOut = dataIn*.3048
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "ft" .or. \
        unitsIn .eq. "feet") then
      if (unitsOut .eq. "mm") then
        dataOut = dataIn*304.8
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "cm") then
        dataOut = dataIn*30.48
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "in") then
        dataOut = dataIn*12
        dataOut@units = unitsOut
      end if
    end if
    if (unitsIn .eq. "in" .or. \
        unitsIn .eq. "inch" .or. \
        unitsIn .eq. "inches") then
      if (unitsOut .eq. "mm") then
        dataOut = dataIn*25.4
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "cm") then
        dataOut = dataIn*2.54
        dataOut@units = unitsOut
      end if
      if (unitsOut .eq. "ft") then
        dataOut = dataIn/12
        dataOut@units = unitsOut
      end if
    end if

    ;; Pressure
    if (unitsIn .eq. "Pa" .or. \
        unitsIn .eq. "kg m-1 s-2") then
      if (unitsOut .eq. "mb" .or.  \
          unitsOut .eq. "mbar" .or. \
          unitsOut .eq. "hPa") then
        dataOut = dataIn/100
        dataOut@units = "hPa"
      end if
    end if
    if (unitsIn .eq. "mb" .or.  \
        unitsIn .eq. "mbar" .or. \
        unitsIn .eq. "hPa") then
      if (unitsOut .eq. "Pa") then
        dataOut = dataIn*100
        dataOut@units = "Pa"
      end if
    end if
  end if
  return dataOut

end

