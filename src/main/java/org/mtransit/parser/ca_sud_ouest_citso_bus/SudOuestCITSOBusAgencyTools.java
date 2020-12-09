package org.mtransit.parser.ca_sud_ouest_citso_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// https://exo.quebec/en/about/open-data
// https://exo.quebec/xdata/citso/google_transit.zip
public class SudOuestCITSOBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-sud-ouest-citso-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new SudOuestCITSOBusAgencyTools().start(args);
	}

	private HashSet<Integer> serviceIds;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating CITSO bus data...");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIdInts(args, this);
		super.start(args);
		MTLog.log("Generating CITSO bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIds != null && this.serviceIds.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTripInt(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		String routeLongName = gRoute.getRouteLongName();
		routeLongName = CleanUtils.SAINT.matcher(routeLongName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		return CleanUtils.cleanLabel(routeLongName);
	}

	private static final Pattern DIGITS = Pattern.compile("[\\d]+");

	@Override
	public long getRouteId(@NotNull GRoute gRoute) {
		//noinspection deprecation
		final String routeId = gRoute.getRouteId();
		if (!Utils.isDigitsOnly(routeId)) {
			Matcher matcher = DIGITS.matcher(routeId);
			if (matcher.find()) {
				return Integer.parseInt(matcher.group());
			}
			throw new MTLog.Fatal("Unexpected route ID for %s!", gRoute);
		}
		return super.getRouteId(gRoute);
	}

	private static final String AGENCY_COLOR = "1F1F1F"; // DARK GRAY (from GTFS)

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		map2.put(31L, new RouteTripSpec(31L, // BECAUSE 1 direction instead of 2
				0, MTrip.HEADSIGN_TYPE_STRING, "Châteauguay", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Montréal") //
				.addTripSort(0, //
						Arrays.asList( //
								"78364", "78904", // Terminus Angrignon
								"78126", // boul. St-Jean Baptiste / boul. St-Francis
								"78135", // boul. St-Jean-Baptiste / face au stationnement inc
								"78043" // boul. St-Joseph / boul. d'Anjou
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"78043", // boul. St-Joseph / boul. d'Anjou
								"78734", // ++
								"78364", "78904" // Terminus Angrignon
						)) //
				.compileBothTripSort());
		map2.put(32L, new RouteTripSpec(32L, // BECAUSE 1 direction instead of 2
				0, MTrip.HEADSIGN_TYPE_STRING, "Châteauguay", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Montréal") //
				.addTripSort(0, //
						Arrays.asList( //
								"78364", "78905", // Terminus Angrignon
								"78315", // ++
								"78128", // boul. St-Francis / boul. St-Jean-Baptiste
								"78171" // boul. d'Anjou / boul. St-Joseph
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"78171", // boul. d'Anjou / boul. St-Joseph
								"78136", // ++
								"78364", "78905" // Terminus Angrignon
						)) //
				.compileBothTripSort());
		map2.put(33L, new RouteTripSpec(33L, // BECAUSE 1 direction instead of 2
				0, MTrip.HEADSIGN_TYPE_STRING, "Faubourg Châteauguay", //
				1, MTrip.HEADSIGN_TYPE_STRING, "Anjou / St-Joseph") //
				.addTripSort(0, //
						Arrays.asList( //
								"78885", // Faubourg Châteauguay
								"78022", // boul. Primeau / rue Principale
								"78043" // boul. St-Joseph / boul. d'Anjou
						)) //
				.addTripSort(1, //
						Arrays.asList( //
								"78043", // boul. St-Joseph / boul. d'Anjou
								"78023", // rue Principale / boul. Primeau
								"78885" // Faubourg Châteauguay
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@NotNull
	@Override
	public String cleanStopOriginalId(@NotNull String gStopId) {
		gStopId = CleanUtils.cleanMergedID(gStopId);
		return gStopId;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsign()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("%s: Using direction finder to merge %s and %s!", mTrip.getRouteId(), mTrip, mTripToMerge);
	}

	private static final Pattern DIRECTION = Pattern.compile("(direction )", Pattern.CASE_INSENSITIVE);
	private static final String DIRECTION_REPLACEMENT = "";

	private static final String STATIONNEMENT_INCITATIF_SHORT = "P+R";
	private static final Pattern STATIONNEMENT_INCITATIF_ = Pattern.compile("((^|\\W)(stationnement incitatif)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String STATIONNEMENT_INCITATIF_REPLACEMENT = "$2" + STATIONNEMENT_INCITATIF_SHORT + "$4";

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = DIRECTION.matcher(tripHeadsign).replaceAll(DIRECTION_REPLACEMENT);
		tripHeadsign = STATIONNEMENT_INCITATIF_.matcher(tripHeadsign).replaceAll(STATIONNEMENT_INCITATIF_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern START_WITH_FACE_A = Pattern.compile("^(face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
	private static final Pattern START_WITH_FACE_AU = Pattern.compile("^(face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE = Pattern.compile("^(face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern SPACE_FACE_A = Pattern.compile("( face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
	private static final Pattern SPACE_WITH_FACE_AU = Pattern.compile("( face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE = Pattern.compile("( face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern[] START_WITH_FACES = new Pattern[]{START_WITH_FACE_A, START_WITH_FACE_AU, START_WITH_FACE};

	private static final Pattern[] SPACE_FACES = new Pattern[]{SPACE_FACE_A, SPACE_WITH_FACE_AU, SPACE_WITH_FACE};

	private static final Pattern AVENUE = Pattern.compile("( avenue)", Pattern.CASE_INSENSITIVE);
	private static final String AVENUE_REPLACEMENT = " av.";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = AVENUE.matcher(gStopName).replaceAll(AVENUE_REPLACEMENT);
		gStopName = Utils.replaceAll(gStopName, START_WITH_FACES, CleanUtils.SPACE);
		gStopName = Utils.replaceAll(gStopName, SPACE_FACES, CleanUtils.SPACE);
		return CleanUtils.cleanLabelFR(gStopName);
	}

	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";

	private static final String ZERO = "0";

	@NotNull
	@Override
	public String getStopCode(@NotNull GStop gStop) {
		if (ZERO.equals(gStop.getStopCode())) {
			return null;
		}
		return super.getStopCode(gStop);
	}

	@Override
	public int getStopId(@NotNull GStop gStop) {
		String stopCode = getStopCode(gStop);
		if (stopCode != null && stopCode.length() > 0 && Utils.isDigitsOnly(stopCode)) {
			return Integer.parseInt(stopCode); // using stop code as stop ID
		}
		//noinspection deprecation
		final String stopId1 = gStop.getStopId();
		Matcher matcher = DIGITS.matcher(stopId1);
		if (matcher.find()) {
			int digits = Integer.parseInt(matcher.group());
			int stopId;
			if (stopId1.startsWith("KAH")) {
				stopId = 1_000_000;
			} else {
				throw new MTLog.Fatal("Stop doesn't have an ID (start with)! %s", gStop);
			}
			if (stopId1.endsWith(A)) {
				stopId += 1000;
			} else if (stopId1.endsWith(B)) {
				stopId += 2000;
			} else if (stopId1.endsWith(C)) {
				stopId += 3000;
			} else if (stopId1.endsWith(D)) {
				stopId += 4000;
			} else {
				throw new MTLog.Fatal("Stop doesn't have an ID (end with)! %s!", gStop);
			}
			return stopId + digits;
		}
		throw new MTLog.Fatal("Unexpected stop ID for %s!", gStop);
	}
}
