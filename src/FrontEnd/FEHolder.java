package FrontEnd;

/**
* FrontEnd/FEHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Users/quocminhvu/Documents/workspace/IdeaProjects/COMP6231_Project/src/FrontEnd.idl
* Friday, July 28, 2017 2:41:09 o'clock PM EDT
*/

public final class FEHolder implements org.omg.CORBA.portable.Streamable
{
  public FrontEnd.FE value = null;

  public FEHolder ()
  {
  }

  public FEHolder (FrontEnd.FE initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FrontEnd.FEHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FrontEnd.FEHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FrontEnd.FEHelper.type ();
  }

}
