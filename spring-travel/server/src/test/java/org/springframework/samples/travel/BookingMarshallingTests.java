package org.springframework.samples.travel;

public class BookingMarshallingTests {

/*	public static void main(String args[]) throws Throwable {

		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(RestConfiguration.class);
		Jaxb2Marshaller jaxb2Marshaller = applicationContext.getBean(Jaxb2Marshaller.class);


		Hotel hotel = new Hotel();
		hotel.setAddress("12332 Street St");
		hotel.setCity("Los Angeles, CA");
		hotel.setCountry("USA");
		hotel.setPrice(new BigDecimal(242));
		hotel.setId((long) (Math.random() * 2302));
		hotel.setZip("90210");
		hotel.setName("The Hotel");

		User u = new User();
		u.setName("Name Of User");
		u.setPassword("PassWord");
		u.setUsername("UserName");

		Booking booking = new Booking();
		booking.setAmenities(new HashSet<Amenity>(Arrays.asList(Amenity.LATE_CHECKOUT, Amenity.MINIBAR)));
		booking.setBeds(2);
		booking.setHotel(hotel);
		booking.setId(242L);
		booking.setSmoking(false);
		booking.setCheckinDate(new Date());
		booking.setUser(u);
		booking.setCheckoutDate(new Date());

		String hotelString = asString(jaxb2Marshaller, booking);

		System.out.println(hotelString);
	}

	private static String asString(Marshaller m, Object graph) throws Throwable {
		StringWriter w = null;
		try {
			w = new StringWriter();
			Result result = new StreamResult(w);
			m.marshal(graph, result);
		} finally {
			if (w != null) {
				w.close();
			}
		}
		return w.toString();
	}*/
}
