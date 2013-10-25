package in.co.madhur.mapmylocation.location;

public class Coordinates
{
	public static final Coordinates UNDEFINED = null;
	private double latitude;
	private double longitude;
	
	public Coordinates(double d, double e)
	{
		this.latitude=d;
		this.longitude=e;
		
	}
	
	public double getLatitude()
	{
		return latitude;
		
	}
	
	
	
	public double getLongitude()
	{
		return longitude;
		
	}
	
	 @Override
	public String toString()
	{
 
		return "Coordinates: "+String.valueOf(latitude) +" "+ String.valueOf(longitude);
	}
}
