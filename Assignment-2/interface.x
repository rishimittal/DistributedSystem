program RPC{
	version INTERFACE{
		int FILEWRITE64K(string filename<>, long offset, string data<>) = 1;

		long NUMFILECHUNKS(string filename<>) = 2;

		int FILEREAD64K(string filename<>,long offset,string dataRead<>) = 3;
	} = 1;

} = 0x20134088 ;

