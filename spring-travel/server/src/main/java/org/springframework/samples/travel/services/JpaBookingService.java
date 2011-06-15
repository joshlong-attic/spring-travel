package org.springframework.samples.travel.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.samples.travel.domain.Booking;
import org.springframework.samples.travel.domain.Hotel;
import org.springframework.samples.travel.domain.User;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * A JPA 2-based implementation of the {@link BookingService} .
 * Delegates to a JPA {@link EntityManager} to issue data access calls against the backing database.
 * <p/>
 * The EntityManager reference is provided by the managing container (Spring) automatically.
 * <p/>
 * This class specifically avoids delegating to a separate {@link Repository}, as such a composition would
 * be redundant in this case. There is no need for the extra level of indirection, especially given how high-level JPA already is.
 * Indeed, such a composition would be largely a formality.
 */
@Service("bookingService")
@SuppressWarnings("unchecked")
public class JpaBookingService implements BookingService {


	/**
	 * Region names
	 */
	static final private String HOTELS_REGION = "hotels";
	static final private String BOOKING_REGION = "bookings";
	static final private String USER_REGION = "users";

	private EntityManager em;

	private Log log = LogFactory.getLog(getClass());

	@Transactional(readOnly = true)
	public User findUserById(Long id) {
		return em.find(User.class, id);
	}

	@Transactional(readOnly = true)
	public Booking findBookingById(Long id) {
		return em.find(Booking.class, id);
	}

	/**
	 * this is provided by the {@link org.springframework.orm.jpa.LocalEntityManagerFactoryBean} which is configured by the managing container (Spring).
	 *
	 * @param em the thread-safe, delegating {@link EntityManager} proxy.
	 */
	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}


	@Transactional(readOnly = true)
	public List<Booking> findBookings(String username) {
		if (username != null) {
			return em.createQuery("select b from Booking b where b.user.username = :username order by b.checkinDate").setParameter("username", username).getResultList();
		} else {
			return null;
		}
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Hotel> findHotels(SearchCriteria criteria) {

		String pattern = getSearchPattern(criteria);

		log.debug("searching hotels with search pattern: " + pattern);

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<Hotel> hotelCriteriaQuery = criteriaBuilder.createQuery(Hotel.class);

		Root<Hotel> from = hotelCriteriaQuery.from(Hotel.class);

		Expression<String> city = from.get("city");
		Expression<String> zip = from.get("zip");
		Expression<String> address = from.get("address");
		Expression<String> name = from.get("name");
		Expression<Double> price = from.get("price");

		Predicate predicate = criteriaBuilder.or(
				                                        criteriaBuilder.like(criteriaBuilder.lower(city), pattern),
				                                        criteriaBuilder.like(criteriaBuilder.lower(zip), pattern),
				                                        criteriaBuilder.like(criteriaBuilder.lower(address), pattern),
				                                        criteriaBuilder.like(criteriaBuilder.lower(name), pattern));

		if (criteria.getMaximumPrice() > 0) {
			predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(price, criteria.getMaximumPrice()));
		}

		hotelCriteriaQuery.where(predicate);

		TypedQuery<Hotel> typedQuery = em.createQuery(hotelCriteriaQuery);

		if (criteria.getPage() > 0 && criteria.getPageSize() > 0)
			typedQuery.setMaxResults(criteria.getPageSize()).setFirstResult(criteria.getPage() * criteria.getPageSize());

		List<Hotel> hotels = typedQuery.getResultList();

		log.debug("returned " + hotels.size() + " results");
		return hotels;
	}


	@Cacheable(value = HOTELS_REGION)
	@Transactional(readOnly = true)
	public Hotel findHotelById(Long id) {
		return em.find(Hotel.class, id);
	}

	@Cacheable(value = BOOKING_REGION, key = "#0")
	@Transactional
	public Booking createBooking(Long hotelId, String username) {
		Hotel hotel = em.find(Hotel.class, hotelId);
		User user = findUser(username);
		Booking booking = new Booking(hotel, user);
		em.persist(booking);
		return booking;
	}


	@Override
	@Transactional
	public void persistBooking(Booking booking) {
		em.merge(booking);
	}

	@CacheEvict(BOOKING_REGION)
	@Transactional
	public void cancelBooking(Long id) {
		Booking booking = em.find(Booking.class, id);
		if (booking != null) {
			em.refresh(booking);
			em.remove(booking);
		}
	}

	// helpers
	private String getSearchPattern(SearchCriteria criteria) {
		if (StringUtils.hasText(criteria.getSearchString())) {
			return "%" + criteria.getSearchString().toLowerCase().replace('*', '%') + "%";
		} else {
			return "'%'";
		}
	}

	@Cacheable( value = USER_REGION ,key = "#0")
	public User findUser(String username) {
		return (User) em.createQuery( "select u from User u where u.username = :username")
						  .setParameter("username", username)
						  .getSingleResult();
	}

	@Override
	@Cacheable( value = USER_REGION ,key = "#0")
	public User login(String u, String pw) {
		return findUser(u);
	}
}
