package atlasService;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import atlasTools.IsraelCoordinatesTransformations;

public class LocalizationsReader extends DBConnection {
	
	private DBConnection connection;
	
	public LocalizationsReader()
	{
	}
	
	public ArrayList<Localization> getLocalizations(int count, long tag) throws SQLException
	{
		if (count < 1)
			return new ArrayList<Localization>();

		try {
			
			connection = new DBConnection();
			connection.Connect();
			
			if (!connection.isConnected())
			{
				return null;
			}
			
			String query = (tag >= 0) ? 
				"SELECT * FROM LOCALIZATIONS WHERE TAG=" + tag
				+ " ORDER BY TIME DESC LIMIT " + count :
				"SELECT * FROM LOCALIZATIONS ORDER BY TIME DESC LIMIT " + count;
			
			Statement st = connection.mConn.createStatement();
			ResultSet rs = st.executeQuery(query);
			
			ArrayList<Localization> ret = ParseResult(rs);
			st.close();
			
			return ret;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		finally {
			if (connection.isConnected())
				connection.mConn.close();
		}

	}
	
	private ArrayList<Localization> ParseResult(ResultSet rs) throws SQLException
	{
		ArrayList<Localization> result = new ArrayList<Localization>();
		while (rs.next()) {
			double[] wgs84 = IsraelCoordinatesTransformations
					.itm2wgs84(new double[] { rs.getDouble("Y"),
							rs.getDouble("X") });
			String sql_time = rs.getString("TIME");
			String sql_milisecond = (sql_time.indexOf('.') < 0 || sql_time.indexOf('.') ==sql_time.length()-1) ?
					"000000":sql_time.substring(sql_time.indexOf('.')+1);
			sql_time = sql_time.substring(0,
					sql_time.indexOf('.') <= 0 ?
						sql_time.length() - 1
						: sql_time.indexOf('.'));
			
//			long mili=0;
			long time_parse = 0;
			try{
			 time_parse = Long.parseLong(sql_time);
			}
			catch(Exception e)
			{
				time_parse=0;
			}
			try{
				if(sql_milisecond.length()>3)
					sql_milisecond=sql_milisecond.substring(0,3);
				else if (sql_milisecond.length()==2)
					sql_milisecond+="0";
				else if (sql_milisecond.length()==1)
					sql_milisecond+="00";
				else if (sql_milisecond.length()==0)
					sql_milisecond+="000";
			}
			catch(Exception e)
			{
				
			}
			
			Date time = null;
			if (time_parse != 0)
				time = new Date((time_parse * 1000L));
			Localization tmp = new Localization(rs.getLong("ID"),
					rs.getLong("TAG"), rs.getLong("TX"),
					rs.getString("TIME"), rs.getDouble("X"),
					rs.getDouble("Y"), rs.getDouble("Z"),
					rs.getInt("STATIONS_NUM"), wgs84[0], wgs84[1], time);
			result.add(tmp);
		}
		
		return result;

	}

}
