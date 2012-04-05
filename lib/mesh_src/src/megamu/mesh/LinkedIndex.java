package megamu.mesh;

public class LinkedIndex {

	 LinkedArray array;
	 int index;
	 int[] links;
	 int linkCount;

	 public LinkedIndex(LinkedArray a, int i){
		  array = a;
		  index = i;
		  links = new int[1];
		  linkCount = 0;
	 }

	 public void linkTo(int i){
		  if( links.length == linkCount ) {
				int[] links2 = new int[links.length + 1];
				System.arraycopy(links, 0, links2, 0, links.length);
				links = links2;
		  }
		  links[linkCount++] = i;
	 }

	 public boolean linked(int i){
		  for(int j=0; j<linkCount; j++)
				if(links[j]==i)
					 return true;
		  return false;
	 }

	 public int[] getLinks(){
		  return links;
	 }

}
